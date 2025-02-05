package com.example.usedauction.service;

import com.example.usedauction.model.User;
import com.example.usedauction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User addUser(User user) {
        // 비밀번호 해싱
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        // 해싱된 비밀번호를 저장하여 사용자 추가
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    // 사용자 정보를 업데이트하는 메서드
    public Optional<User> updateUser(String id, Map<String, Object> updates) {
        Optional<User> userOptional = userRepository.findById(id); // ID로 사용자 검색

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 업데이트할 필드를 설정
            if (updates.containsKey("nickname")) {
                user.setNickname((String) updates.get("nickname"));
            }

            // 필요한 경우 다른 필드들도 업데이트

            userRepository.save(user); // 변경된 사용자 정보 저장
            return Optional.of(user);
        } else {
            return Optional.empty(); // 사용자가 없는 경우 빈 Optional 반환
        }
    }

}
