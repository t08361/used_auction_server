package com.example.usedauction.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final byte[] secretKey; // 변경: 바이트 배열로 변경하여 키 저장

    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds;

    // 생성자를 통해 초기화
    public JwtTokenProvider(@Value("${security.jwt.token.secret-key:secret-key}") String secretKeyProperty) {
        // 강력한 키 생성 (32자 이상 비밀 키 사용)
        if (secretKeyProperty.length() < 32) {
            throw new IllegalArgumentException("The secret key must be at least 32 characters long");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretKeyProperty.getBytes()).getEncoded();
    }

    public String createToken(String email) {
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(Keys.hmacShaKeyFor(secretKey), SignatureAlgorithm.HS256) // 강력한 키로 서명
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder() // parserBuilder 사용
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Expired or invalid JWT token");
        }
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder() // parserBuilder 사용
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String resolveToken(HttpServletRequest req) {
        String bearerToken = req.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
