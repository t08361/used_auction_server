package com.example.usedauction.controller;

import com.example.usedauction.model.User;
import com.example.usedauction.repository.UserRepository;
import com.example.usedauction.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public AuthController(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/social-login")
    public ResponseEntity<?> socialLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        try {
            // Google ID Token 검증
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                    .setAudience(Collections.singletonList("882104702900-n52kolmobiihpmap1gjdso5qr27p5fcj.apps.googleusercontent.com"))  // 여기에 Google OAuth 클라이언트 ID 입력
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();

                // 사용자 정보 추출
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleId = payload.getSubject();

                // 사용자 정보가 DB에 존재하는지 확인 (이메일 기준)
                Optional<User> userOptional = userRepository.findByEmail(email);
                User user;

                if (userOptional.isPresent()) {
                    user = userOptional.get();
                } else {
                    // 사용자가 존재하지 않으면 새로 저장
                    user = new User(googleId, name, email);
                    userRepository.save(user);
                }

                // JWT 토큰 생성
                String jwtToken = jwtTokenProvider.createToken(email);

                // 사용자 정보와 JWT 토큰을 클라이언트에 반환
                Map<String, Object> response = new HashMap<>();
                response.put("user", user);
                response.put("token", jwtToken);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid ID token.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Server error: " + e.getMessage());
        }
    }
}
