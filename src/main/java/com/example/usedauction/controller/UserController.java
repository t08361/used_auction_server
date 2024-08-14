package com.example.usedauction.controller;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.usedauction.model.User; // User 모델 클래스를 import
import com.example.usedauction.service.UserService; // UserService 클래스를 import (비즈니스 로직을 담당)
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 파싱을 위해 ObjectMapper를 import
import org.springframework.beans.factory.annotation.Autowired; // 의존성 주입을 위해 Autowired를 import
import org.springframework.http.HttpStatus; // HTTP 상태 코드를 사용하기 위해 import
import org.springframework.http.ResponseEntity; // HTTP 응답을 만들기 위해 ResponseEntity를 import
import org.springframework.web.bind.annotation.*; // REST 컨트롤러 관련 어노테이션을 import
import org.springframework.web.multipart.MultipartFile; // 파일 업로드를 처리하기 위해 MultipartFile을 import
import java.io.IOException; // IO 예외 처리를 위해 IOException을 import
import java.util.Base64; // Base64 인코딩을 위해 import
import java.util.List; // 리스트 데이터 구조를 사용하기 위해 import
import java.util.Map;
import java.util.Optional; // Optional을 사용하여 null 안전성을 제공하기 위해 import

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<User> addUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "profile_image", required = false) MultipartFile profileImage) {

        User user;
        try {
            user = new ObjectMapper().readValue(userJson, User.class);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (profileImage == null) {
            System.out.println("프로필 이미지 없음");
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String base64Image = Base64.getEncoder().encodeToString(profileImage.getBytes());
                user.setProfileImage(base64Image);
                System.out.println("Profile Image Set: " + base64Image);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        User newUser = userService.addUser(user);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User loginUser) {
        Optional<User> user = userService.getUserByEmail(loginUser.getEmail());

        if (user.isPresent() && passwordEncoder.matches(loginUser.getPassword(), user.get().getPassword())) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // 사용자 정보를 업데이트하는 PATCH 요청 처리
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // UserService를 통해 사용자 정보 업데이트
        Optional<User> updatedUser = userService.updateUser(id, updates);

        // 업데이트가 성공했으면 사용자 정보 반환, 그렇지 않으면 404 Not Found 반환
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
