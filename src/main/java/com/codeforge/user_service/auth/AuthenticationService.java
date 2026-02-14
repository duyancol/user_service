package com.codeforge.user_service.auth;


import com.codeforge.user_service.exception.CustomerNotFoundException;
import com.codeforge.user_service.config.JwtService;
//import com.codeforge.user_service.modelEmail.Email;
//import com.codeforge.user_service.modelEmail.EmailUntilt;
import com.codeforge.user_service.token.Token;
import com.codeforge.user_service.token.TokenRepository;
import com.codeforge.user_service.token.TokenType;
import com.codeforge.user_service.user.Role;
import com.codeforge.user_service.user.User;
import com.codeforge.user_service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
//    if(user==null){
//      var jwtToken = jwtService.generateToken(user);
//      revokeAllUserTokens(user);
//      saveUserToken(user, jwtToken);
//      return AuthenticationResponse.builder()
//              .token(jwtToken)
//              .type("Beerer")
//              .id(user.getId())
//              .email(user.getEmail())
//              .firstname(user.getFirstname())
//              .lastname(user.getLastname())
//              .roles(user.getAuthorities())
//
//              .build();
//    }
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .type("Beerer")
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .roles(user.getAuthorities())

                .build();
        //  return null;
    }
    public void updateUserRole(String email, String newRole) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

//    Role role;
//    try {
//      role = Role.valueOf(newRole.toUpperCase()); // Chuyển đổi chuỗi thành enum Role
//    } catch (IllegalArgumentException e) {
//     e.getMessage();
//    }

        user.setRole(Role.valueOf(newRole)); // Gán enum Role vào trường role
        repository.save(user);
    }
    public void updateUserSetting(String email, String firtName,String lastName,String phone,String address) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));



        user.setFirstname(firtName);
        user.setLastname(lastName);
        user.setPhone(phone);
        user.setAddress(address);

        repository.save(user);
    }
    public List<AuthenticationResponse> findAll() {
        List<User> users = repository.findAll();
        List<AuthenticationResponse> responses = new ArrayList<>();

        for (User user : users) {
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token("your_token_value")
                    .type("Bearer")
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .roles(user.getAuthorities())
                    .build();

            responses.add(response);
        }

        return responses;
    }
    public void updatePassword(String token, String newPassword) {
        User user = repository.findByResetPasswordToken(token);

        user.setPassword(passwordEncoder.encode(newPassword));

        user.setResetPasswordToken(null);
        repository.save(user);
    }
    public void updateResetPasswordToken(String token, String email) throws CustomerNotFoundException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user != null) {
            user.setResetPasswordToken(token);
            repository.save(user);
            System.out.println("da up date" + token);
        } else {
            throw new CustomerNotFoundException("Could not find any customer with the email " + email);
        }
    }
    //  public class RandomStringGenerator {
//    public static void main(String[] args) {
//      int length = 45;
//      String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
//      StringBuilder randomString = new StringBuilder();
//      Random random = new Random();
//      for (int i = 0; i < length; i++) {
//        randomString.append(chars.charAt(random.nextInt(chars.length())));
//      }
//      System.out.println("Random string: " + randomString.toString());
//    }
//  }
    public String RandomStringGenerator(int n){
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String sum="";
        StringBuilder randomString = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            sum= randomString.append(chars.charAt(random.nextInt(chars.length()))).toString();
        }
        return sum;
    }
//    public String processForgotPasswordForm(String email){
//
//        String token= RandomStringGenerator(45);
//        try{
//            updateResetPasswordToken(token,email);
//            String resetPassWord= "https://webbangiay-woad.vercel.app/reset_password/"+token;
//            Email email1 = new Email();
//            email1.setFrom("nguyenduy.30719@gmail.com");
//            email1.setFromPss("iuntfgqwytqmwsvr");
//            email1.setTo(email);
//            email1.setSubject(" :");
//            StringBuilder sb= new StringBuilder();
//            sb.append("<p>Hello,</p>\n" +
//                    "             \"<p>You have requested to reset your password.</p>\"\n" +
//                    "             \"<p>Click the link below to change your password:</p>\"\n" +
//                    "             \"<p><a href=\"" + resetPassWord + "\">Change my password</a></p>\"\n" +
//                    "             \"<br>\"\n" +
//                    "             \"<p>Ignore this email if you do remember your password, \"\n" +
//                    "             \"or you have not made the request.</p>\"").append("<br>");
//
//
//            email1.setContent(sb.toString());
//            try {
//                EmailUntilt.send(email1);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//
//        }catch (Exception e){
//            e.getMessage();
//        }
//        return email+" "+token;
//    }
    public AuthenticationResponse getUserByID(String email) {

        var user = repository.findByEmail(email)
                .orElseThrow();

        return AuthenticationResponse.builder()
                .token("")
                .type("Beerer")
                .id(user.getId())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .address(user.getAddress())
                .phone(user.getPhone())
                .roles(user.getAuthorities())

                .build();
    }

    public AuthenticationResponse authenticate1(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .type("Beerer")
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .roles(user.getAuthorities())

                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
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
}

