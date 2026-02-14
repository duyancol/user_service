package com.codeforge.user_service.auth;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@Builder


public class AuthenticationResponse {
    private  long id;
    private String token;
    private String type ="Beerer";
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String resetPasswordToken;
    private String phone;
    private String address;
    private Collection<? extends GrantedAuthority> roles;

    public AuthenticationResponse(long id, String token, String type, String firstname, String lastname, String email, String password,String resetPasswordToken,String phone,String address, Collection<? extends GrantedAuthority> roles) {
        this.id = id;
        this.token = token;
        this.type = type;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.resetPasswordToken = resetPasswordToken;
        this.phone = phone;
        this.address = address;
        this.roles = roles;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Collection<? extends GrantedAuthority> getRoles() {
        return roles;
    }

    public void setRoles(Collection<? extends GrantedAuthority> roles) {
        this.roles = roles;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }
}
