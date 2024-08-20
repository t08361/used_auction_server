package com.example.usedauction.configuration;

import com.example.usedauction.security.JwtRequestFilter;
import com.example.usedauction.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@Configuration // 이 클래스가 Spring 설정 클래스임을 나타냄
@EnableWebSecurity // Spring Security를 활성화
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final CustomUserDetailsService customUserDetailsService;

    // 생성자 주입을 통해 JwtRequestFilter와 CustomUserDetailsService를 주입받음
    @Autowired
    public SecurityConfig(@Lazy JwtRequestFilter jwtRequestFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    // 비밀번호 암호화를 위한 PasswordEncoder 빈을 생성
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCryptPasswordEncoder를 사용하여 비밀번호를 암호화
    }

    // SecurityFilterChain 빈을 설정하여 HTTP 요청에 대한 보안 설정을 정의
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (JWT를 사용하는 경우 일반적으로 비활성화)
                .csrf(csrf -> csrf.disable())

                // 요청에 대한 인가 정책을 설정
                .authorizeHttpRequests(authz -> authz
                        // .requestMatchers("/api/auth/items/**").authenticated(): /api/items/** 경로에 대한 요청은 인증된 사용자만 접근 가능
                        .anyRequest().permitAll() // 나머지 모든 요청은 인증 없이 접근 가능
                )

                // 세션 관리 정책 설정 (JWT 사용 시 무상태로 설정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 생성하지 않도록 설정
                )

                // JwtRequestFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 설정을 마친 SecurityFilterChain을 반환
    }
}
