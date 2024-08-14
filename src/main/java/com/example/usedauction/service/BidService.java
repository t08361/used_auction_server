package com.example.usedauction.service;

import com.example.usedauction.model.Bid; // 입찰자 정보를 위한 모델 클래스
import com.example.usedauction.model.User; // 사용자 정보를 위한 모델 클래스
import com.example.usedauction.model.Item; // 상품 정보를 위한 모델 클래스
import com.example.usedauction.repository.BidRepository; // 입찰 정보 저장소
import com.example.usedauction.repository.UserRepository; // 사용자 정보 저장소
import com.example.usedauction.repository.ItemRepository; // 상품 정보 저장소
import org.springframework.beans.factory.annotation.Autowired; // 의존성 주입을 위한 어노테이션
import org.springframework.stereotype.Service; // 서비스 클래스를 나타내는 어노테이션

import java.util.Date; // 날짜와 시간을 다루기 위한 클래스
import java.util.List; // 리스트로 다루기 위한 인터페이스
import java.util.stream.Collectors; // 스트림 API를 사용하기 위한 클래스

@Service // Spring의 서비스 레이어를 나타내는 어노테이션
public class BidService {

    private final BidRepository bidRepository; // 입찰 관련 데이터베이스 접근을 위한 저장소
    private final UserRepository userRepository; // 사용자 관련 데이터베이스 접근을 위한 저장소
    private final ItemRepository itemRepository; // 상품 관련 데이터베이스 접근을 위한 저장소

    @Autowired // 의존성 주입을 통해 저장소들을 초기화
    public BidService(BidRepository bidRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    // 새로운 입찰 기록을 저장하는 메서드
    public Bid placeBid(String itemId, String bidderId, int bidAmount) {
        Bid bid = new Bid(); // 새로운 Bid 객체 생성
        bid.setItemId(itemId); // 입찰한 상품의 ID 설정
        bid.setBidderId(bidderId); // 입찰자의 ID 설정
        bid.setBidAmount(bidAmount); // 입찰 금액 설정
        bid.setBidTime(new Date()); // 입찰 시간을 현재 시간으로 설정
        Bid savedBid = bidRepository.save(bid); // 입찰 기록을 데이터베이스에 저장

        // 상품의 현재 최고가 업데이트
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item을 찾을 수 없음")); // 상품이 존재하지 않을 경우 예외 처리
        item.setLastPrice(bidAmount); // 상품의 현재 최고가를 입찰 금액으로 설정
        itemRepository.save(item); // 업데이트된 상품 정보를 데이터베이스에 저장

        return savedBid; // 저장된 입찰 기록 반환
    }

    // 모든 입찰 기록을 가져오는 메서드
    public List<Bid> getAllBids() {
        return bidRepository.findAll(); // 모든 입찰 기록을 데이터베이스에서 조회하여 반환
    }

    // 특정 상품의 입찰 기록을 가져오는 메서드(입찰자의 id를 통해 닉네임 가져오기)
    public List<BidWithNickname> getBidsByItemId(String itemId) {
        List<Bid> bids = bidRepository.findByItemId(itemId); // 현재 상품 ID를 통해 입찰 기록을 가져옴
        return bids.stream()
                .map(bid -> {
                    User user = userRepository.findById(bid.getBidderId()).orElse(null); // 입찰자의 id를 통해 입찰자 정보를 가져옴
                    String nickname = user != null ? user.getNickname() : "알수없음"; // 닉네임 설정
                    return new BidWithNickname(bid, nickname); // Bid 정보와 닉네임을 합쳐 새로운 객체로 반환
                })
                .collect(Collectors.toList()); // 결과를 리스트로 수집하여 반환
    }

    // Bid와 닉네임을 포함하는 내부 클래스 (상품 상세 페이지에서 입찰 기록을 가져올 때 사용)
    public static class BidWithNickname {
        private final Bid bid; // 입찰 정보
        private final String nickname; // 입찰자의 닉네임

        // 생성자를 통해 Bid 정보와 닉네임을 초기화
        public BidWithNickname(Bid bid, String nickname) {
            this.bid = bid;
            this.nickname = nickname;
        }

        // Bid 정보를 반환하는 메서드
        public Bid getBid() {
            return bid;
        }

        // 닉네임을 반환하는 메서드
        public String getNickname() {
            return nickname;
        }
    }
}
