package com.codeforge.user_service.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    public User findByResetPasswordToken(String token);
    List<User> findByRole(Role role);

    // Lấy danh sách user có role chính xác

    // Lấy danh sách user theo nhiều role
    List<User> findByRoleIn(List<Role> roles);
    Page<User> findByRole(Role role, Pageable pageable);


}
