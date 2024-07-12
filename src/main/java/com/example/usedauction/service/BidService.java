package com.example.usedauction.service;

import com.example.usedauction.model.Bid;
import com.example.usedauction.model.User; // 사용자 정보도 필요함
import com.example.usedauction.model.Item; // 상품 정보도 필요함
import com.example.usedauction.repository.BidRepository;
import com.example.usedauction.repository.UserRepository;
import com.example.usedauction.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BidService(BidRepository bidRepository, UserRepository userRepository, ItemRepository itemRepository) {
        this.bidRepository = bidRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    // 입찰 기록을 저장하는 메서드
    public Bid placeBid(String itemId, String bidderId, int bidAmount) {
        Bid bid = new Bid();
        bid.setItemId(itemId);
        bid.setBidderId(bidderId);
        bid.setBidAmount(bidAmount);
        bid.setBidTime(new Date());
        Bid savedBid = bidRepository.save(bid); // 입찰기록 저장

        // 상품 collection의 현재 최고가도 업데이트 시켜줘야함
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item을 찾을 수 없음"));
        item.setLastPrice(bidAmount);
        itemRepository.save(item);

        return savedBid;
    }

    // 모든 입찰 기록을 가져오는 메서드
    public List<Bid> getAllBids() {
        return bidRepository.findAll();
    }

    // 특정 상품의 입찰 기록을 가져오는 메서드(입찰자의id를 통해 닉네임 가져오기)
    public List<BidWithNickname> getBidsByItemId(String itemId) {
        List<Bid> bids = bidRepository.findByItemId(itemId); // 현재 상품 ID 를 통해 입찰 기록을 가져옴
        return bids.stream()
            .map(bid -> {
                User user = userRepository.findById(bid.getBidderId()).orElse(null); // 입찰자의 id를 통해 입찰자 정보를 가져옴
                String nickname = user != null ? user.getNickname() : "알수없음"; // 닉네임 설정
                return new BidWithNickname(bid, nickname); // Bid 정보와 닉네임을 합쳐 새로운 객체로 반환
            })
            .collect(Collectors.toList());
    }

    // Bid와 닉네임을 포함하는 내부 클래스 (상품 상세 페이지에서 입찰기록을 가져올 때 사용)
    public static class BidWithNickname {
        private Bid bid;
        private String nickname;

        public BidWithNickname(Bid bid, String nickname) {
            this.bid = bid;
            this.nickname = nickname;
        }

        public Bid getBid() {return bid;}

        public String getNickname() {return nickname;}
    }
}
