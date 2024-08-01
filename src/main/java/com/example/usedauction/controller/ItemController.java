package com.example.usedauction.controller;

import com.example.usedauction.model.Item; // Item 모델을 import
import com.example.usedauction.service.ItemService; // Item 서비스 클래스
import org.springframework.beans.factory.annotation.Autowired; // @Autowired 어노테이션을 import
import org.springframework.http.HttpStatus; // HTTP 상태 코드를 import
import org.springframework.http.ResponseEntity; // HTTP 응답을 처리하기 위한 클래스를 import
import org.springframework.web.bind.annotation.*; // @RestController, @RequestMapping 등을 import

import java.time.Duration;
import java.time.LocalDateTime; // 날짜 및 시간 처리를 위한 클래스를 import
import java.time.format.DateTimeFormatter; // 날짜 및 시간 형식을 처리하기 위한 클래스를 import
import java.util.Arrays;  // 배열 작업을 위한 클래스
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

    // 새로운 아이템을 추가하는 HTTP POST 요청을 처리 (multipart/form-data 형식으로 데이터 받음)
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> addItem(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") int price,
            @RequestParam("endDateTime") String endDateTime,
            @RequestParam("bidUnit") int bidUnit,
            @RequestParam("userId") String userId,
            @RequestParam("nickname") String nickname,
            @RequestParam("region") String region,
            @RequestParam(value = "itemImages", required = false) String[] itemImages // 이미지 URL 배열
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME; // 날짜 및 시간 형식 설정
            LocalDateTime parsedEndDateTime = LocalDateTime.parse(endDateTime, formatter); // 문자열을 LocalDateTime으로 변환

            Item newItem = new Item();  // 새로운 아이템 객체 생성
            newItem.setTitle(title);
            newItem.setDescription(description);
            newItem.setPrice(price);
            newItem.setEndDateTime(parsedEndDateTime);
            newItem.setBidUnit(bidUnit);
            newItem.setUserId(userId);
            newItem.setLastPrice(0); // 초기 입찰가는 0으로 설정
            newItem.setRegion(region);

            if (itemImages != null && itemImages.length > 0) {
                newItem.setItemImages(Arrays.asList(itemImages)); // 이미지 URL 배열 설정
            }

            itemService.addItem(newItem); // 새로운 아이템을 서비스 계층을 통해 저장

            return new ResponseEntity<>("Item added successfully", HttpStatus.CREATED); // 성공 메시지와 함께 201 Created 반환
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to add item: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // 오류 메시지와 함께 500 Internal Server Error 반환
        }
    }

    // 경매 우승자를 업데이트하는 HTTP PUT 요청을 처리
    @PutMapping("/{itemId}/winningBid")
    public ResponseEntity<Void> updateWinner(
            @PathVariable String itemId, // 경로 변수로 아이템 ID를 받음
            @RequestBody Map<String, Object> updates) { // 요청 본문에서 업데이트할 데이터를 받음
        int lastPrice = (int) updates.get("lastPrice");  // 마지막 입찰가를 추출
        String winnerId = (String) updates.get("winnerId"); // 우승자 ID를 추출
        itemService.updateWinner(itemId, winnerId, lastPrice); // 우승자 정보 업데이트
        return ResponseEntity.ok().build(); // 200 OK 상태 반환
    }

    // 특정 아이템을 삭제하는 HTTP DELETE 요청을 처리
    @DeleteMapping("/{id}") // 특정 아이템 ID로 아이템을 삭제
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id); // 특정 아이템 ID로 아이템을 삭제
        return ResponseEntity.noContent().build(); // 삭제 후 204 No Content 상태를 반환
    }

    // 특정 아이템의 정보를 업데이트하는 HTTP PUT 요청을 처리
    @PutMapping("/{id}")
    public ResponseEntity<String> updateItem(
            @PathVariable String id,
            @RequestBody Map<String, String> payload) {  // 요청 본문에서 업데이트할 데이터를 받음
        try {
            String title = payload.get("title");  // 제목을 추출
            String description = payload.get("description"); // 설명을 추출
            String region = payload.get("region"); // 지역을 추출
            itemService.updateItem(id, title, description, region); // 아이템 정보 업데이트
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 스택 추적 출력
            return new ResponseEntity<>("fail: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 특정 아이템의 현재 가격을 조회하는 HTTP GET 요청을 처리
    @GetMapping("/{id}/current_price")
    public ResponseEntity<Integer> getCurrentPrice(@PathVariable String id) {
        try {
            int currentPrice = itemService.getCurrentPrice(id); // 현재 가격 조회
            return ResponseEntity.ok(currentPrice); // 현재 가격을 200 OK와 함께 반환
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 아이템이 없으면 404 Not Found 반환
        }
    }

    // 특정 아이템의 남은 시간을 조회하는 HTTP GET 요청을 처리
    @GetMapping("/{id}/remaining_time")
    public ResponseEntity<Long> getRemainingTime(@PathVariable String id) {
        try {
            Duration remainingTime = itemService.getRemainingTime(id); // 남은 시간 계산
            return ResponseEntity.ok(remainingTime.toMinutes()); // 남은 시간을 분 단위로 반환
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();  // 아이템이 없으면 404 Not Found 반환
        }
    }
}
