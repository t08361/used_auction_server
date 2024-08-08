package com.example.usedauction.controller;

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
import java.util.Optional; // Optional을 사용하여 null 안전성을 제공하기 위해 import

// REST API 컨트롤러임을 나타내며, "UserController" 클래스를 정의
@RestController
// "/api/users" 경로로 매핑하여 사용자 관련 API 엔드포인트를 정의
@RequestMapping("/api/users")
public class UserController {

    // UserService를 자동으로 주입하여 사용 (의존성 주입)
    @Autowired
    private UserService userService;

    // 모든 사용자를 가져오는 GET 요청을 처리
    @GetMapping
    public List<User> getAllUsers() {
        // UserService의 getAllUsers 메서드를 호출하여 모든 사용자 목록을 반환
        return userService.getAllUsers();
    }

    // 특정 ID를 가진 사용자를 가져오는 GET 요청을 처리
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        // UserService의 getUserById 메서드를 호출하여 Optional<User> 객체를 반환
        Optional<User> user = userService.getUserById(id);
        // 사용자가 존재하면 OK 상태 코드와 함께 반환, 그렇지 않으면 404 Not Found 응답 생성
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 새로운 사용자를 추가하는 POST 요청을 처리
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<User> addUser(
            @RequestPart("user") String userJson, // JSON 형식의 사용자 데이터를 받음
            @RequestPart(value = "profile_image", required = false) MultipartFile profileImage) { // 선택적으로 프로필 이미지를 받음

        User user;
        try {
            // ObjectMapper를 사용하여 JSON 문자열을 User 객체로 변환
            user = new ObjectMapper().readValue(userJson, User.class);
        } catch (IOException e) {
            // JSON 변환이 실패하면 400 Bad Request 상태 코드 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // 프로필 이미지가 없으면 콘솔에 메시지 출력 (디버그 목적)
        if (profileImage == null) {
            System.out.println("프로필 이미지 없음");
        }
        // 프로필 이미지가 존재하고 비어있지 않다면
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 이미지 파일을 Base64로 인코딩하여 문자열로 변환
                String base64Image = Base64.getEncoder().encodeToString(profileImage.getBytes());
                // 변환된 이미지를 User 객체에 설정
                user.setProfileImage(base64Image);
                System.out.println("Profile Image Set: " + base64Image); // 디버그 목적의 메시지 출력
            } catch (IOException e) {
                // 이미지 처리 중 예외 발생 시 500 Internal Server Error 상태 코드 반환
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // UserService를 사용해 새로운 사용자를 추가하고, 결과를 newUser에 저장
        User newUser = userService.addUser(user);
        // 생성된 사용자와 함께 201 Created 상태 코드를 반환
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // 특정 ID를 가진 사용자를 삭제하는 DELETE 요청을 처리
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        // UserService를 사용해 사용자를 삭제
        userService.deleteUser(id);
        // 삭제 성공 시 204 No Content 상태 코드 반환 (내용 없이 성공)
        return ResponseEntity.noContent().build();
    }

    // 사용자의 로그인 요청을 처리하는 POST 요청
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User loginUser) {
        // UserService를 사용해 이메일로 사용자 검색
        Optional<User> user = userService.getUserByEmail(loginUser.getEmail());
        // 사용자가 존재하고 비밀번호가 일치하면
        if (user.isPresent() && user.get().getPassword().equals(loginUser.getPassword())) {
            // OK 상태 코드와 함께 사용자 정보 반환
            return ResponseEntity.ok(user.get());
        } else {
            // 그렇지 않으면 401 Unauthorized 상태 코드 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
