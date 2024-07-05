// ItemService.java
// 이 클래스는 아이템(Item) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.

package com.example.usedauction.service;

import com.example.usedauction.model.Item; // Item 모델을 임포트
import com.example.usedauction.repository.ItemRepository; // Item 리포지토리를 임포트
import org.springframework.beans.factory.annotation.Autowired; // Autowired 애너테이션을 임포트
import org.springframework.stereotype.Service; // Service 애너테이션을 임포트

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
}