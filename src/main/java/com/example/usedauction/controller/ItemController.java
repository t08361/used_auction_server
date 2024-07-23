package com.example.usedauction.controller;

import com.example.usedauction.model.Item; // Item 모델을 import
import com.example.usedauction.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired; // @Autowired 어노테이션을 import
import org.springframework.http.HttpStatus; // HTTP 상태 코드를 import
import org.springframework.http.ResponseEntity; // HTTP 응답을 처리하기 위한 클래스를 import
import org.springframework.web.bind.annotation.*; // @RestController, @RequestMapping 등을 import
import org.springframework.web.multipart.MultipartFile; // 파일 업로드를 위한 클래스를 import

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime; // 날짜 및 시간 처리를 위한 클래스를 import
import java.time.format.DateTimeFormatter; // 날짜 및 시간 형식을 처리하기 위한 클래스를 import
import java.util.Base64; // Base64 인코딩을 위한 클래스를 import
import java.util.List; // 리스트 처리를 위한 클래스를 import
import java.util.Map;
import java.util.Optional; // Optional 클래스를 import

@RestController // 이 클래스가 RESTful 웹 서비스의 컨트롤러임을 나타냄
@RequestMapping("/api/items") // 이 컨트롤러의 기본 URL 경로를 설정
public class ItemController {
    @Autowired // 스프링이 ItemService의 인스턴스를 자동으로 주입
    private ItemService itemService;

    @GetMapping // HTTP GET 요청을 처리
    public List<Item> getAllItems() {
        return itemService.getAllItems(); // 모든 아이템을 조회하여 반환
    }

    @GetMapping("/{id}") // HTTP GET 요청을 처리하며 경로 변수로 아이템 ID를 받음
    public ResponseEntity<Item> getItemById(@PathVariable String id) {
        Optional<Item> item = itemService.getItemById(id); // 특정 아이템 ID로 아이템을 조회
        return item.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build()); // 아이템이 존재하면 반환, 없으면 404 상태 반환
    }

    @PostMapping(consumes = {"multipart/form-data"}) // HTTP POST 요청을 처리하며 multipart/form-data 형식을 받음
    public ResponseEntity<String> addItem(
            @RequestParam("title") String title, // 제목 파라미터를 받음
            @RequestParam("description") String description, // 설명 파라미터를 받음
            @RequestParam("price") int price, // 가격 파라미터를 받음
            @RequestParam("endDateTime") String endDateTime, // 종료 시간 파라미터를 받음
            @RequestParam("bidUnit") int bidUnit, // 입찰 단위 파라미터를 받음
            @RequestParam("userId") String userId, // 상품등록자의 아이디 파라미터를 받음
            @RequestParam("nickname") String nickname, // 상품등록자의 닉네임 파라미터를 받음
            @RequestParam(value = "itemImage", required = false) String itemImage // 이미지 URL 파라미터를 받음
    ) {
        try {
            // 날짜 및 시간 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME; // ISO 형식의 날짜 및 시간 포맷터 생성
            LocalDateTime parsedEndDateTime = LocalDateTime.parse(endDateTime, formatter); // 문자열을 LocalDateTime으로 변환

            // 아이템 객체 생성 및 데이터베이스에 저장
            Item newItem = new Item(); // 새로운 아이템 객체 생성
            newItem.setTitle(title); // 제목 설정
            newItem.setDescription(description); // 설명 설정
            newItem.setPrice(price); // 가격 설정
            newItem.setEndDateTime(parsedEndDateTime); // 종료 시간 설정
            newItem.setBidUnit(bidUnit); // 입찰 단위 설정
            newItem.setUserId(userId); // 사용자 ID 설정
            newItem.setLastPrice(0); // 현재 최고가 설정

            if (itemImage != null && !itemImage.isEmpty()) {
                newItem.setItemImage(itemImage); // 이미지 URL 설정
            }

            itemService.addItem(newItem); // 새로운 아이템을 데이터베이스에 저장

            return new ResponseEntity<>("Item added successfully", HttpStatus.CREATED); // 성공적으로 추가되었음을 클라이언트에 응답
        } catch (Exception e) {
            e.printStackTrace(); // 예외 스택 트레이스를 출력
            return new ResponseEntity<>("Failed to add item: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 오류 발생 시 클라이언트에 오류 메시지 응답
        }
    }

    @PutMapping("/{itemId}/winningBid")
    public ResponseEntity<Void> updateWinner(
            @PathVariable String itemId,
            @RequestBody Map<String, Object> updates) {
        int lastPrice = (int) updates.get("lastPrice");
        String winnerId = (String) updates.get("winnerId");
        itemService.updateWinner(itemId, winnerId, lastPrice);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}") // HTTP DELETE 요청을 처리하며 경로 변수로 아이템 ID를 받음
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id); // 특정 아이템 ID로 아이템을 삭제
        return ResponseEntity.noContent().build(); // 삭제 후 204 No Content 상태를 반환
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateItem(
            @PathVariable String id,
            @RequestBody Map<String,String> payload)
    {
        try{
            String title = payload.get("title");
            String description = payload.get("description");
            itemService.updateItem(id, title, description);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch(Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("fail" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/current_price")
    public ResponseEntity<Integer> getCurrentPrice(@PathVariable String id) {
        try {
            int currentPrice = itemService.getCurrentPrice(id);
            return ResponseEntity.ok(currentPrice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/remaining_time")
    public ResponseEntity<Long> getRemainingTime(@PathVariable String id) {
        try {
            Duration remainingTime = itemService.getRemainingTime(id);
            return ResponseEntity.ok(remainingTime.toMinutes());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
