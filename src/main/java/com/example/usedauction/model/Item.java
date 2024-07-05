// Item.java
// 이 클래스는 MongoDB 컬렉션 "items"에 저장되는 아이템의 모델을 정의합니다.

package com.example.usedauction.model;

import org.springframework.data.annotation.Id; // MongoDB의 ID 필드를 정의하기 위해 임포트
import org.springframework.data.mongodb.core.mapping.Document; // MongoDB 문서 매핑을 위해 임포트
import java.time.LocalDateTime; // 날짜 및 시간 필드를 위해 임포트

@Document(collection = "items") // 이 클래스가 MongoDB 컬렉션 "items"에 매핑됨을 나타냄
public class Item {
    @Id // MongoDB의 기본 키를 나타냄
    private String id; // 아이템의 고유 ID
    private String title; // 아이템의 제목
    private String description; // 아이템의 설명
    private int price; // 아이템의 가격
    private LocalDateTime  endDateTime; // 아이템 경매의 종료 시간
    private int bidUnit; // 아이템의 입찰 단위
    //private String imagePath; // 이미지 파일 경로 (주석 처리됨)
    private String userId; // 상품등록자의 아이디
    private String nickname; // 상품등록자의 닉네임
    // Getters and setters

    public String getId() {
        return id; // ID 값을 반환
    }

    public void setId(String id) {
        this.id = id; // ID 값을 설정
    }

    public String getTitle() {
        return title; // 제목 값을 반환
    }

    public void setTitle(String title) {
        this.title = title; // 제목 값을 설정
    }

    public String getDescription() {
        return description; // 설명 값을 반환
    }

    public void setDescription(String description) {
        this.description = description; // 설명 값을 설정
    }

    public int getPrice() {
        return price; // 가격 값을 반환
    }

    public void setPrice(int price) {
        this.price = price; // 가격 값을 설정
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime; // 종료 시간 값을 반환
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime; // 종료 시간 값을 설정
    }

    public int getBidUnit() {
        return bidUnit; // 입찰 단위 값을 반환
    }

    public void setBidUnit(int bidUnit) {
        this.bidUnit = bidUnit; // 입찰 단위 값을 설정
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

//    public String getImagePath() {
//        return imagePath; // 이미지 파일 경로 값을 반환
//    }
//
//    public void setImagePath(String imagePath) {
//        this.imagePath = imagePath; // 이미지 파일 경로 값을 설정
//    }
}