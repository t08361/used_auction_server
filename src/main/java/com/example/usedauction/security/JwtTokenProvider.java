package com.example.usedauction.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest; // javax.servlet에서 jakarta.servlet로 변경
import java.util.Date;

@Component
public class JwtTokenProvider { // 토큰을 생성하고 검증하는 기능

    private final byte[] secretKey; // 비밀 키를 바이트 배열로 저장

    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds; // 토큰의 유효기간 (기본값: 1시간)

    // 생성자를 통해 초기화
    public JwtTokenProvider(@Value("${security.jwt.token.secret-key:secret-key}") String secretKeyProperty) {
        // 비밀 키가 32자 이상인지 확인하고, HMAC-SHA 키로 변환하여 저장
        if (secretKeyProperty.length() < 32) {
            throw new IllegalArgumentException("The secret key must be at least 32 characters long");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretKeyProperty.getBytes()).getEncoded();
    }

    // JWT 토큰 생성 메서드
    public String createToken(String email) {
        // 클레임(Claims) 생성: 토큰의 payload 부분에 들어가는 정보
        Claims claims = Jwts.claims().setSubject(email); // 사용자의 이메일을 서브젝트로 설정
        Date now = new Date(); // 현재 시간
        Date validity = new Date(now.getTime() + validityInMilliseconds); // 토큰의 만료 시간 설정

        // 토큰을 빌드하고 서명하여 반환
        return Jwts.builder()
                .setClaims(claims) // 클레임 설정
                .setIssuedAt(now) // 토큰 발급 시간 설정
                .setExpiration(validity) // 토큰 만료 시간 설정
                .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS256) // 비밀 키로 서명
                .compact(); // 토큰 문자열 생성
    }

    // JWT 토큰의 유효성을 검사하는 메서드
    public boolean validateToken(String token) {
        try {
            // 토큰을 파싱하고 서명 검증을 통해 유효성을 검사
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey) // 서명 키 설정
                    .build()
                    .parseClaimsJws(token); // 토큰 파싱 및 서명 검증
            return !claims.getBody().getExpiration().before(new Date()); // 만료 여부 확인
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않은 경우 예외를 던짐
            throw new RuntimeException("Expired or invalid JWT token");
        }
    }

    // JWT 토큰에서 사용자 이메일(서브젝트)을 추출하는 메서드
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey) // 서명 키 설정
                .build()
                .parseClaimsJws(token) // 토큰 파싱
                .getBody()
                .getSubject(); // 서브젝트(이메일) 반환
    }

    // HTTP 요청에서 JWT 토큰을 추출하는 메서드
    public String resolveToken(HttpServletRequest req) {
        // HTTP Authorization 헤더에서 토큰을 가져옴
        String bearerToken = req.getHeader("Authorization");
        // 토큰이 "Bearer "로 시작하면, 그 이후의 문자열을 반환
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null; // 토큰이 없거나 형식이 맞지 않으면 null 반환
    }
}
