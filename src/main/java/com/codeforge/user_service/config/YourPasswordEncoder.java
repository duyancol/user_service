package com.codeforge.user_service.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class YourPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public YourPasswordEncoder() {
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public String encode(CharSequence rawPassword) {

        return bCryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {

        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}
