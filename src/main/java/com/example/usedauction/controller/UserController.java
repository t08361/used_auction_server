package com.example.usedauction.controller;

import com.example.usedauction.security.JwtTokenProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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


    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<User> getAllUsers() {
        // 인증된 사용자의 정보를 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        // 인증 정보에서 사용자의 주체(Principal)를 가져옵니다.
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        System.out.println("Authenticated user's email: " + email);

        return userService.getAllUsers(); // 모든 사용자 정보를 반환
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        // 인증된 사용자의 정보를 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        // 인증 정보에서 사용자의 주체(Principal)를 가져옵니다.
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        System.out.println("Authenticated user's email: " + email);

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

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        // 인증된 사용자의 정보를 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        // 인증 정보에서 사용자의 주체(Principal)를 가져옵니다.
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        System.out.println("Authenticated user's email: " + email);

        // 추가적인 검증 로직: 예를 들어, 인증된 사용자가 본인의 계정만 삭제할 수 있게 할 수 있음
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent() && !user.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한이 없는 경우 403 Forbidden 반환
        }

        userService.deleteUser(id); // 사용자 삭제
        return ResponseEntity.noContent().build(); // 성공 시 204 No Content 반환
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



    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 인증된 사용자의 정보를 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        // 인증 정보에서 사용자의 주체(Principal)를 가져옵니다.
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        System.out.println("Authenticated user's email: " + email);

        // 추가적인 검증 로직: 예를 들어, 인증된 사용자가 본인의 계정만 수정할 수 있게 할 수 있음
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent() && !user.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한이 없는 경우 403 Forbidden 반환
        }

        // 사용자 업데이트
        Optional<User> updatedUser = userService.updateUser(id, updates);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
