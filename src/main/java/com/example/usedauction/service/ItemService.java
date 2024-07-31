package com.example.usedauction.service;

import com.example.usedauction.model.Item; // Item 모델을 임포트
import com.example.usedauction.repository.ItemRepository; // Item 리포지토리를 임포트
import org.springframework.beans.factory.annotation.Autowired; // Autowired 애너테이션을 임포트
import org.springframework.stereotype.Service; // Service 애너테이션을 임포트

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List; // 리스트를 임포트
import java.util.Optional; // Optional을 임포트

@Service // 이 클래스가 서비스 레이어의 빈(Bean)임을 나타냄
public class ItemService {

    @Autowired // 스프링이 ItemRepository의 인스턴스를 자동으로 주입
    private ItemRepository itemRepository;

    // 모든 아이템을 조회하여 반환하는 메서드
    public List<Item> getAllItems() {
        return itemRepository.findAll(); // 리포지토리의 findAll 메서드를 호출하여 모든 아이템을 조회
    }

    // 특정 아이템 ID로 아이템을 조회하는 메서드
    public Optional<Item> getItemById(String id) {
        return itemRepository.findById(id); // 리포지토리의 findById 메서드를 호출하여 특정 아이템을 조회
    }

    // 새로운 아이템을 추가하는 메서드
    public Item addItem(Item item) {
        return itemRepository.save(item); // 리포지토리의 save 메서드를 호출하여 새로운 아이템을 저장
    }

    // 특정 아이템 ID로 아이템을 삭제하는 메서드
    public void deleteItem(String id) {
        itemRepository.deleteById(id); // 리포지토리의 deleteById 메서드를 호출하여 특정 아이템을 삭제
    }

    // 특정 아이템의 정보를 업데이트하는 메서드
    public Item updateItem(String id, String title, String description, String region) {
        // 기존 아이템을 찾아 Optional로 반환
        Optional<Item> optionalItem = itemRepository.findById(id);

        // 기존 아이템이 존재하는 경우 수정 후 저장
        if (optionalItem.isPresent()) {
            Item existingItem = optionalItem.get();
            existingItem.setTitle(title); // 제목 수정
            existingItem.setDescription(description); // 설명 수정
            existingItem.setRegion(region); // 지역 수정

            return itemRepository.save(existingItem); // 수정된 아이템을 저장
        } else {
            throw new RuntimeException("Item not found with id " + id); // 아이템을 찾지 못했을 경우 예외 발생
        }
    }

    // 경매 우승자를 업데이트하는 메서드
    public void updateWinner(String itemId, String winnerId, int lastPrice) {
        Optional<Item> optionalItem = itemRepository.findById(itemId); // 아이템 ID로 아이템을 조회
        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Item not found"); // 아이템을 찾지 못했을 경우 예외 발생
        }

        Item item = optionalItem.get();
        item.setWinnerId(winnerId); // 우승자 ID 설정
        item.setLastPrice(lastPrice); // 마지막 가격 설정
        itemRepository.save(item); // 수정된 아이템을 저장
    }

    // 특정 아이템의 현재 가격을 조회하는 메서드
    public int getCurrentPrice(String itemId) {
        return itemRepository.findById(itemId)
                .map(Item::getLastPrice)  // 아이템의 마지막 가격을 가져옴
                .orElseThrow(() -> new RuntimeException("Item not found"));  // 아이템을 찾지 못했을 경우 예외 발생
    }

    // 특정 아이템의 남은 시간을 조회하는 메서드
    public Duration getRemainingTime(String itemId) {
        return itemRepository.findById(itemId)
                .map(item -> {
                    LocalDateTime endDateTime = item.getEndDateTime();  // 아이템의 경매 종료 시간을 가져옴
                    if (endDateTime == null) {
                        throw new RuntimeException("End date time is null for item: " + itemId); // 종료 시간이 없을 경우 예외 발생
                    }
                    return Duration.between(LocalDateTime.now(), endDateTime); // 현재 시간과 종료 시간의 차이를 계산하여 반환
                })
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));  // 아이템을 찾지 못했을 경우 예외 발생
    }
}
