package com.example.usedauction.service;

import com.example.usedauction.model.User;
import com.example.usedauction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이메일로 사용자 검색
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 비밀번호가 null인 경우 더미 비밀번호 설정
        String password = user.getPassword() != null ? user.getPassword() : "{noop}dummy-password";

        // User 객체를 Spring Security의 UserDetails로 변환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password) // 더미 비밀번호 설정
                .authorities("USER")  // 권한 설정
                .build();
    }
}