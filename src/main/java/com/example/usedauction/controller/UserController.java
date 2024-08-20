package com.example.usedauction.controller;

import com.example.usedauction.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.usedauction.model.User;
import com.example.usedauction.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // 여기에 JwtTokenProvider 주입

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
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User loginUser) {
        Optional<User> user = userService.getUserByEmail(loginUser.getEmail());

        if (user.isPresent() && passwordEncoder.matches(loginUser.getPassword(), user.get().getPassword())) {
            String token = jwtTokenProvider.createToken(user.get().getEmail());

            // 사용자 정보 콘솔에 출력
//            System.out.println("User ID: " + user.get().getId());
//            System.out.println("Nickname: " + user.get().getNickname());
//            System.out.println("Email: " + user.get().getEmail());
//            System.out.println(token);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }



    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Optional<User> updatedUser = userService.updateUser(id, updates);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
