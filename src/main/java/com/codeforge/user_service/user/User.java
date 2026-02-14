package com.codeforge.user_service.user;


import com.codeforge.user_service.token.Token;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Integer id;
    private String userId;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String address;
    private String phone;
    private String resetPasswordToken;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;

    public User(String userId, String firstname, String email) {
        this.userId = userId;
        this.firstname = firstname;
        this.email = email;
    }

    public User(String userId, String firstname, String email, Role role) {
        this.userId = userId;
        this.firstname = firstname;
        this.email = email;
        this.role = role;
    }

    public User(Integer id, String userId, String firstname, String email, Role role) {
        this.id = id;
        this.userId = userId;
        this.firstname = firstname;
        this.email = email;
        this.role = role;
    }

    public User(String firstname, String lastname, String email, String address, String phone) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.address = address;
        this.phone = phone;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }


    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
