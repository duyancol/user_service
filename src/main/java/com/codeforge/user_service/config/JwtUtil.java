package com.codeforge.user_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
@Component
public class JwtUtil {
    private final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"; // Thay thế bằng một khóa bí mật phức tạp hơn

//    public String generateToken(String subject) {
//        Date now = new Date();
//        Date expirationDate = new Date(now.getTime() + 86400000); // Thời gian hết hạn của token (1 ngày)
//
//        Map<String, Object> claims = new HashMap<>();
//        // Thêm các thông tin khác vào claims (nếu có)
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(subject)
//                .setIssuedAt(now)
//                .setExpiration(expirationDate)
//                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public String extractUserId(String token) {
//        Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
//        return claims.getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
}


