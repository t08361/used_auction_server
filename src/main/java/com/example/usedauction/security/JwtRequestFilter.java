package com.example.usedauction.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // @Lazy를 사용하여 UserDetailsService의 의존성 주입을 지연시킴
    // 이렇게 하면, 애플리케이션 시작 시점에서의 순환 의존성 문제를 피할 수 있음
    public JwtRequestFilter(JwtTokenProvider jwtTokenProvider, @Lazy UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 요청에서 JWT 토큰을 추출
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰이 존재하고 유효한지 확인
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 이메일(사용자 이름) 추출
            String email = jwtTokenProvider.getEmail(token);

            // UserDetailsService를 사용하여 사용자 세부 정보를 로드
            var userDetails = userDetailsService.loadUserByUsername(email);

            // 사용자 정보가 유효한지 확인
            if (userDetails != null) {
                // 사용자 인증 토큰을 생성하여 SecurityContext에 설정
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 현재 인증된 사용자 정보를 SecurityContextHolder에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청을 전달
        chain.doFilter(request, response);
    }
}
