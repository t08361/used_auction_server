package com.example.usedauction.controller;

import com.example.usedauction.model.Bid; // 입찰 정보를 위한 모델 클래스
import com.example.usedauction.service.BidService; // 입찰 관련 비즈니스 로직을 처리하는 서비스 클래스
import com.example.usedauction.service.BidService.BidWithNickname; // 입찰과 닉네임 정보를 함께 제공하는 클래스
import org.springframework.beans.factory.annotation.Autowired; // 스프링의 의존성 주입을 위한 어노테이션
import org.springframework.http.HttpStatus; // HTTP 상태 코드를 관리하기 위한 클래스
import org.springframework.http.ResponseEntity; // HTTP 응답을 나타내기 위한 클래스
import org.springframework.web.bind.annotation.*;

import java.util.List; // 리스트를 사용하기 위한 임포트

@RestController // 이 클래스가 RESTful 웹 서비스의 컨트롤러임을 나타냄
@RequestMapping("/api/bids") // 이 컨트롤러가 처리하는 기본 요청 경로 설정
public class BidController {

    private final BidService bidService; // BidService를 사용하기 위한 필드

    @Autowired // 스프링이 BidService의 인스턴스를 자동으로 주입
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    // 새로운 입찰 정보를 저장하는 엔드포인트
    @PostMapping(consumes = {"application/json"})  // POST 요청으로 JSON 형식의 데이터를 받음
    public ResponseEntity<Bid> placeBid(@RequestBody Bid bid) {
        // BidService를 통해 입찰 정보 저장
        Bid placedBid = bidService.placeBid(bid.getItemId(), bid.getBidderId(), bid.getBidAmount());
        // 저장된 입찰 정보를 HTTP 상태 코드 201 (Created)와 함께 반환
        return new ResponseEntity<>(placedBid, HttpStatus.CREATED);
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
