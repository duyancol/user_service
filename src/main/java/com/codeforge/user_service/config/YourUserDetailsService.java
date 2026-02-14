package com.codeforge.user_service.config;

import com.codeforge.user_service.user.User;
import com.codeforge.user_service.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class YourUserDetailsService implements UserDetailsService {

    // Giả sử bạn có một lớp UserRepository để truy xuất thông tin người dùng từ cơ sở dữ liệu
    private final UserRepository userRepository;

    public YourUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm kiếm thông tin người dùng trong cơ sở dữ liệu bằng email hoặc tên người dùng (username)
        Optional<User> userEntityOptional = userRepository.findByEmail(username);

        if (userEntityOptional.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + username);
        }

        User userEntity = userEntityOptional.get();

        // Tạo một đối tượng UserDetails từ thông tin người dùng lấy từ cơ sở dữ liệu
        return User.builder()
                .firstname(userEntity.getFirstname())
                .password(userEntity.getPassword())

                .build();
    }
}

