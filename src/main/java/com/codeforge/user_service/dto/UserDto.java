package com.codeforge.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String address;
    private String role;
}