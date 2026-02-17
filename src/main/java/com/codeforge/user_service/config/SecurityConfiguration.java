package com.codeforge.user_service.config;


import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfiguration {
    //    private final JwtUtil jwtUtil;
//    private final YourUserDetailsService userDetailsService;
//    private final YourPasswordEncoder passwordEncoder;
//
//
//
//    private final JwtAuthenticationFilter jwtAuthFilter;
//  private final AuthenticationProvider authenticationProvider;
//  private final LogoutHandler logoutHandler;
    private final JwtUtil jwtUtil;
    private final YourUserDetailsService userDetailsService;
    private final YourPasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final GoogleIdTokenFilter googleIdTokenFilter;
    private final LogoutHandler logoutHandler;
    private final AuthenticationProvider authenticationProvider;



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//        .csrf()
//        .disable()
                .authorizeHttpRequests()
//            .requestMatchers("/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/auth/users/userAll").hasAuthority("ADMIN")
                .requestMatchers("/api/v1/auth/users/updateUserSetting").hasAuthority("USER")
                .requestMatchers("/api/v1/cart/**").hasAuthority("USER")
                .requestMatchers("/images/img/**").permitAll()
                .requestMatchers("/api/v1/auth/google").permitAll()
                .requestMatchers("/api/inventory/**").permitAll()
                .requestMatchers("/api/player/**").permitAll()
                .requestMatchers("/api/equipment/**").permitAll()
                .requestMatchers("/api/v1/auth/**")
                .permitAll()

                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(googleIdTokenFilter,UsernamePasswordAuthenticationFilter.class)
        ;

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(googleIdTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors().and().csrf().disable();


        ;

        return http.build();
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/api/**")
//                        .allowedOrigins("http://localhost:3000", "http://localhost:5173")
//                        .allowedMethods("GET", "POST", "PUT", "DELETE")
//                        .allowedHeaders("*");
//            }
//        };
//    }
//@Bean
//public CorsConfigurationSource corsConfigurationSource() {
//    CorsConfiguration config = new CorsConfiguration();
//    config.setAllowedOrigins(List.of(
//            "http://localhost:5173",
//            "https://toeic.codejud.id.vn"
//    ));
//    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//    config.setAllowedHeaders(List.of("*"));
//    config.setAllowCredentials(true); // rất quan trọng nếu bạn dùng cookie hoặc Authorization
//
//    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", config);
//    return source;
//}

}

