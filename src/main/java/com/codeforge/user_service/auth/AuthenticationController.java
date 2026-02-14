package com.codeforge.user_service.auth;

import com.codeforge.user_service.config.JwtService;
import com.codeforge.user_service.dto.UserDto;

import com.codeforge.user_service.token.Token;
import com.codeforge.user_service.token.TokenRepository;
import com.codeforge.user_service.token.TokenType;
import com.codeforge.user_service.user.Role;
import com.codeforge.user_service.user.User;
import com.codeforge.user_service.user.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")

@RequiredArgsConstructor
public class AuthenticationController {
    private final UserRepository repository;

    private final AuthenticationService service;
    @Autowired AuthenticationService authenticationService;

    private final TokenRepository tokenRepository;
    @Autowired
    private final JwtService jwtService;

    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        String googleIdToken = body.get("idToken");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList("272854032499-uvoh7etrb27k4sp664qd3baj900l703l.apps.googleusercontent.com"))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify(googleIdToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String userId = payload.getSubject();
                String email = payload.getEmail();
                String fullName = (String) payload.get("name");

                // ✅ Phân biệt user mới hay đã tồn tại
                Optional<User> optionalUser = repository.findByEmail(email);
                User user;
                boolean isNewUser;

                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                    isNewUser = false;
                } else {
                    user = repository.save(User.builder()
                            .userId(userId)
                            .firstname(fullName)
                            .email(email)
                            .role(Role.USER)
                            .build());
                    isNewUser = true;
                }



                String clientIp = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");

                // ✅ Access token hiện tại (giữ nguyên logic generateToken1)
                String accessToken = jwtService.generateToken1(new HashMap<>(), user, clientIp, userAgent);

                // ✅ Refresh token mới
                String refreshToken = jwtService.generateRefreshToken(user);

                // (OPTIONAL nhưng nên có) Thu hồi token cũ của user nếu bạn có hàm này
                 revokeAllUserTokens(user);

                // ✅ Lưu cả access & refresh token vào DB
                saveUserToken(user, accessToken, TokenType.BEARER);
                saveUserToken(user, refreshToken, TokenType.REFRESH);

                // ✅ Cookie access_token (sống ngắn)
                ResponseCookie accessCookie = ResponseCookie.from("access_token", accessToken)
                        .httpOnly(true)
                        .secure(true)          // HTTPS
 //                       .secure(false)          // local
                        .path("/")
                        .maxAge(Duration.ofMinutes(181)) // tuỳ bạn: 15 / 30 / 60 phút
                        .sameSite("None")
 //                       .sameSite("Lax")        // ⬅ DEV: Lax thôi
                        .build();

                // ✅ Cookie refresh_token (sống dài hơn)
                ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                        .httpOnly(true)
                        .secure(true)
 //                       .secure(false)          // local
                        .path("/")
                        .maxAge(Duration.ofMinutes(60*24*7))
                        .sameSite("None")
 //                       .sameSite("Lax")        // ⬅ DEV: Lax thôi
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                        .body(Map.of(
                                "id", user.getId(),
                                "firstname", user.getFirstname(),
                                "email", user.getEmail()
                        ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Google token.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;

        // Lấy refresh_token từ cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Không có refresh token"));
        }

        try {
            String email = jwtService.extractUsername(refreshToken);
            var user = repository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check JWT hợp lệ (chưa hết hạn, subject khớp user)
            if (!jwtService.isTokenValid(refreshToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh token không hợp lệ hoặc đã hết hạn"));
            }

            // Check trong DB chưa bị revoke
            Optional<Token> storedTokenOpt = tokenRepository.findByToken(refreshToken);
            if (storedTokenOpt.isEmpty()
                    || storedTokenOpt.get().isExpired()
                    || storedTokenOpt.get().isRevoked()) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Refresh token đã bị thu hồi"));
            }

            String clientIp = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            // Tạo access token mới
            String newAccessToken = jwtService.generateToken1(new HashMap<>(), user, clientIp, userAgent);

            // Lưu access token mới vào DB
            saveUserToken(user, newAccessToken, TokenType.BEARER);

            // Set lại cookie access_token
            ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
 //                   .secure(false)
                    .path("/")
                    .maxAge(Duration.ofMinutes(180)) // đúng với TTL bạn đang test
                    .sameSite("None")
//                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body(Map.of("message", "Refresh token thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token không hợp lệ"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Xóa access_token
        ResponseCookie clearAccess = ResponseCookie.from("access_token", "")
                .httpOnly(true)
 //               .secure(false)      // DEV: trùng với login / refresh-token
                .secure(true)
                .path("/")
                .maxAge(0)          // XÓA
 //               .sameSite("Lax")
                .sameSite("None")
                .build();

        // Xóa refresh_token
        ResponseCookie clearRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
  //              .secure(false)      // DEV
                .secure(true)
                .path("/")
                .maxAge(0)
 //               .sameSite("Lax")
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearAccess.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                .body(Map.of("message", "Logged out successfully"));
    }


//    @GetMapping("/check-auth")
//    public ResponseEntity<?> checkAuth(HttpServletRequest request) {
//        boolean isLoggedIn = false;
//
//        if (request.getCookies() != null) {
//            for (Cookie cookie : request.getCookies()) {
//                if ("access_token".equals(cookie.getName())) {
//                    isLoggedIn = true;
//                    break;
//                }
//            }
//        }
//
//        return ResponseEntity.ok(Map.of("loggedIn", isLoggedIn));
//    }
@GetMapping("/check-auth")
public ResponseEntity<?> checkAuth(HttpServletRequest request) {
    String accessToken = null;
    String refreshToken = null;

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName())) {
                accessToken = cookie.getValue();
            } else if ("refresh_token".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }
    }

    // ❌ Không có token nào -> chưa login
    if (accessToken == null && refreshToken == null) {
        return ResponseEntity.ok(Map.of("loggedIn", false));
    }

    // ✅ Có access_token -> thử verify
    if (accessToken != null) {
        try {
            String email = jwtService.extractUsername(accessToken);
            var user = repository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtService.isTokenValid(accessToken, user)) {
                // Access token còn hạn
                return ResponseEntity.ok(Map.of("loggedIn", true));
            }
        } catch (Exception e) {
            // access invalid / expired -> sẽ thử refresh bên dưới
        }
    }

    // ⬇ Access hết hạn -> thử dùng refresh_token
    if (refreshToken != null) {
        try {
            String email = jwtService.extractUsername(refreshToken);
            var user = repository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Refresh token hết hạn / invalid -> coi như chưa login
            if (!jwtService.isTokenValid(refreshToken, user)) {
                return ResponseEntity.ok(Map.of("loggedIn", false));
            }

            // Refresh còn hạn -> cấp access token mới
            String clientIp = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String newAccessToken = jwtService.generateToken1(new HashMap<>(), user, clientIp, userAgent);

            saveUserToken(user, newAccessToken, TokenType.BEARER);

            ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                    .httpOnly(true)
                   // .secure(false)              // DEV
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(3))  // khớp với JWT access
//                    .sameSite("Lax")
                    .sameSite("None")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body(Map.of("loggedIn", true));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
    }

    // Không access, refresh không dùng được
    return ResponseEntity.ok(Map.of("loggedIn", false));
}


//    private void saveUserToken(User user, String jwtToken) {
//        var token = Token.builder()
//                .user(user)
//                .token(jwtToken)
//                .tokenType(TokenType.BEARER)
//                .expired(false)
//                .revoked(false)
//                .build();

    /// /        System.out.println(token);
//        tokenRepository.save(token);
//    }
    private void saveUserToken(User user, String jwtToken, TokenType type) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(type)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Autowired
    UserRepository userRepository;

    @PutMapping("/users/updateRole")
    public void updateRole(@RequestParam("email") String email, @RequestParam("role") String role) {
        authenticationService.updateUserRole(email, role);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/users/updateUserSetting")
    public void updateUserSetting(@RequestParam("email") String email,
                                  @RequestParam("firstName") String firstName,
                                  @RequestParam("lastName") String lastName,
                                  @RequestParam("phone") String phone,

                                  @RequestParam("address") String address) {
        authenticationService.updateUserSetting(email, firstName, lastName, phone, address);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/userAll")
    public List<AuthenticationResponse> getUserAll() {
        return authenticationService.findAll();

    }

    @GetMapping("/users/getByEmail")
    public AuthenticationResponse getUserByID(@RequestParam("email") String email) {
        return authenticationService.getUserByID(email);

    }


@GetMapping("/check-auth-role")
                public ResponseEntity<?> checkAuthRode(HttpServletRequest request) {
                    Cookie[] cookies = request.getCookies();

                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            if ("access_token".equals(cookie.getName())) {
                                String token = cookie.getValue();
                                try {
                    String email = jwtService.extractUsername(token);
                    String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
                    return ResponseEntity.ok(Map.of(
                            "loggedIn", true,
                            "email", email,
                            "role", role
                    ));
                } catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("loggedIn", false));
                }
            }
        }
    }

    return ResponseEntity.ok(Map.of("loggedIn", false));
}
    @GetMapping("/users/with-role-user")
    public ResponseEntity<?> getAllUsersWithUserRole(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    try {
                        String email = jwtService.extractUsername(token);

                        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
//System.out.println("Role User " +role);
                        // ✅ Kiểm tra quyền: chỉ ADMIN hoặc SUPER_ADMIN được phép
                        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body(Map.of("message", "Không đủ quyền truy cập"));
                        }

                        // ✅ Lấy danh sách user có role USER
                        List<User> users  = userRepository.findByRole(Role.USER);

                        List<UserDto> dtos = users.stream()
                                .map(u -> UserDto.builder()
                                        .id(u.getId())
                                        .firstname(u.getFirstname())
                                        .lastname(u.getLastname())
                                        .email(u.getEmail())
                                        .phone(u.getPhone())
                                        .address(u.getAddress())
                                        .role(u.getRole().name())
                                        .build())
                                .collect(Collectors.toList());
                        return ResponseEntity.ok(dtos);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("loggedIn", false));
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of("loggedIn", false, "message", "Không có token"));
    }
    @GetMapping("/users/with-role-user_page")
    public ResponseEntity<?> getAllUsersWithUserRole(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    try {
                        String email = jwtService.extractUsername(token);
                        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

                        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body(Map.of("message", "Không đủ quyền truy cập"));
                        }

                        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                        Page<User> userPage = userRepository.findByRole(Role.USER, pageable);

                        List<UserDto> dtos = userPage.getContent().stream()
                                .map(u -> UserDto.builder()
                                        .id(u.getId())
                                        .firstname(u.getFirstname())
                                        .lastname(u.getLastname())
                                        .email(u.getEmail())
                                        .phone(u.getPhone())
                                        .address(u.getAddress())
                                        .role(u.getRole().name())
                                        .build())
                                .collect(Collectors.toList());

                        Map<String, Object> response = new HashMap<>();
                        response.put("content", dtos);
                        response.put("currentPage", userPage.getNumber());
                        response.put("totalItems", userPage.getTotalElements());
                        response.put("totalPages", userPage.getTotalPages());

                        return ResponseEntity.ok(response);

                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("loggedIn", false));
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("loggedIn", false, "message", "Không có token"));
    }
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Integer  id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String newRole = body.get("role");
        String providedSecret = body.get("secret");

        // ✅ Lấy token từ cookie
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Không có token"));
        }

        try {
            String requesterEmail = jwtService.extractUsername(token);
            String requesterRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

            // ✅ Kiểm tra quyền SUPER_ADMIN
            if (!"SUPER_ADMIN".equals(requesterRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Chỉ SUPER_ADMIN mới được phép cập nhật role"));
            }

            // ✅ Kiểm tra mã xác thực
            if (!"secret-hiiteach-9999".equals(providedSecret)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Mã xác thực không chính xác"));
            }

            // ✅ Tìm user theo ID (đối tượng bị cập nhật)
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "User không tồn tại"));
            }

            User targetUser = optionalUser.get();

            // ✅ Cập nhật role
            Role parsedRole = Role.valueOf(newRole.toUpperCase());
            targetUser.setRole(parsedRole);
            userRepository.save(targetUser);

            return ResponseEntity.ok(Map.of("message", "Cập nhật role thành công"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role không hợp lệ"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi server", "error", e.getMessage()));
        }
    }

}
