package com.codeforge.user_service.config;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

@Component
public class GoogleIdTokenFilter extends OncePerRequestFilter {
    @Autowired
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public GoogleIdTokenFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String googleIdToken = request.getHeader("Authorization");
        if (googleIdToken != null && googleIdToken.startsWith("Bearer ")) {
            googleIdToken = googleIdToken.substring(7);
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList("272854032499-uvoh7etrb27k4sp664qd3baj900l703l.apps.googleusercontent.com")) // Thay thế bằng Google Client ID của bạn
                    .build();

            try {
                GoogleIdToken idToken = verifier.verify(googleIdToken);
                if (idToken != null) {
                    Payload payload = idToken.getPayload();

                    // Lấy thông tin người dùng từ Payload
                    String userEmail = payload.getEmail();
                    UserDetails userDetails =userDetailsService.loadUserByUsername(userEmail);

                    // Tạo authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Đặt người dùng vào SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Tiếp tục thực hiện các filter tiếp theo
        filterChain.doFilter(request, response);
    }
}


