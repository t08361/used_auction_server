package com.example.usedauction.controller;

import com.example.usedauction.model.Bid; // 입찰 정보를 위한 모델 클래스
import com.example.usedauction.model.User;
import com.example.usedauction.service.BidService; // 입찰 관련 비즈니스 로직을 처리하는 서비스 클래스
import com.example.usedauction.service.BidService.BidWithNickname; // 입찰과 닉네임 정보를 함께 제공하는 클래스
import com.example.usedauction.service.UserService;
import org.springframework.beans.factory.annotation.Autowired; // 스프링의 의존성 주입을 위한 어노테이션
import org.springframework.http.HttpStatus; // HTTP 상태 코드를 관리하기 위한 클래스
import org.springframework.http.ResponseEntity; // HTTP 응답을 나타내기 위한 클래스
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 리스트를 사용하기 위한 임포트
import java.util.Optional;

@RestController // 이 클래스가 RESTful 웹 서비스의 컨트롤러임을 나타냄
@RequestMapping("/api/bids") // 이 컨트롤러가 처리하는 기본 요청 경로 설정
public class BidController {
    @Autowired
    private UserService userService;

    private final BidService bidService; // BidService를 사용하기 위한 필드

    @Autowired // 스프링이 BidService의 인스턴스를 자동으로 주입
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    // 새로운 입찰 정보를 저장하는 엔드포인트

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Bid> placeBid(@RequestBody Bid bid) {
        // 현재 인증된 사용자의 정보를 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        // 인증 정보에서 사용자의 주체(Principal)를 가져옵니다.
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        // 사용자 정보를 이용하여 사용자 ID를 가져옴
        Optional<User> user = userService.getUserByEmail(email);
        if (user.isPresent()) {
            // 인증된 사용자의 ID를 입찰 정보에 설정
            bid.setBidderId(user.get().getId());

            // BidService를 통해 입찰 정보 저장
            Bid placedBid = bidService.placeBid(bid.getItemId(), bid.getBidderId(), bid.getBidAmount());

            // 저장된 입찰 정보를 HTTP 상태 코드 201 (Created)와 함께 반환
            return new ResponseEntity<>(placedBid, HttpStatus.CREATED);
        } else {
            // 인증된 사용자를 찾을 수 없는 경우 404 Not Found 반환
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    // 모든 입찰 기록을 가져오는 엔드포인트
    @GetMapping // GET 요청을 처리하여 모든 입찰 기록을 반환
    public ResponseEntity<List<Bid>> getAllBids() {
        List<Bid> bids = bidService.getAllBids(); // BidService를 통해 모든 입찰 기록을 조회
        return new ResponseEntity<>(bids, HttpStatus.OK); // 조회된 입찰 기록을 HTTP 상태 코드 200 (OK)와 함께 반환
    }

    // 특정 상품의 입찰 기록을 가져오는 엔드포인트
    @GetMapping("/{itemId}")  // GET 요청을 처리하여 특정 상품의 입찰 기록을 반환
    public ResponseEntity<List<BidWithNickname>> getBidsByItemId(@PathVariable String itemId) {
        // BidService를 통해 특정 상품의 입찰 기록과 닉네임 정보를 조회
        List<BidWithNickname> bids = bidService.getBidsByItemId(itemId);
        return new ResponseEntity<>(bids, HttpStatus.OK);  // 조회된 정보를 HTTP 상태 코드 200 (OK)와 함께 반환
    }
}
