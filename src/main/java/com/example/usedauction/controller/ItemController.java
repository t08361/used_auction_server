package com.example.usedauction.controller;

import com.example.usedauction.model.Item; // Item 모델을 import
import com.example.usedauction.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired; // @Autowired 어노테이션을 import
import org.springframework.http.HttpStatus; // HTTP 상태 코드를 import
import org.springframework.http.ResponseEntity; // HTTP 응답을 처리하기 위한 클래스를 import
import org.springframework.web.bind.annotation.*; // @RestController, @RequestMapping 등을 import

import java.time.Duration;
import java.time.LocalDateTime; // 날짜 및 시간 처리를 위한 클래스를 import
import java.time.format.DateTimeFormatter; // 날짜 및 시간 형식을 처리하기 위한 클래스를 import
import java.util.Arrays;
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
            @RequestParam(value = "itemImages", required = false) String[] itemImages
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime parsedEndDateTime = LocalDateTime.parse(endDateTime, formatter);

            Item newItem = new Item();
            newItem.setTitle(title);
            newItem.setDescription(description);
            newItem.setPrice(price);
            newItem.setEndDateTime(parsedEndDateTime);
            newItem.setBidUnit(bidUnit);
            newItem.setUserId(userId);
            newItem.setLastPrice(0);
            newItem.setRegion(region);

            if (itemImages != null && itemImages.length > 0) {
                newItem.setItemImages(Arrays.asList(itemImages)); // 이미지 URL 배열 설정
            }

            itemService.addItem(newItem);

            return new ResponseEntity<>("Item added successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to add item: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
            @RequestBody Map<String,String> payload) {
        try {
            String title = payload.get("title");
            String description = payload.get("description");
            String region = payload.get("region"); // 지역 필드 추가
            itemService.updateItem(id, title, description, region); // 서비스 메서드에 지역 필드 추가
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {

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
