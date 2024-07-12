package com.example.usedauction.service;

import com.example.usedauction.model.Bid;
import com.example.usedauction.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BidService {

    private final BidRepository bidRepository;

    @Autowired
    public BidService(BidRepository bidRepository) {
        this.bidRepository = bidRepository;
    }

    // 입찰 기록을 저장하는 메서드
    public Bid placeBid(String itemId, String bidderId, int bidAmount) {
        Bid bid = new Bid();
        bid.setItemId(itemId);
        bid.setBidderId(bidderId);
        bid.setBidAmount(bidAmount);
        bid.setBidTime(new Date());
        return bidRepository.save(bid);
    }

    // 특정 상품의 입찰 기록을 가져오는 메서드
    public List<Bid> getBidsByItemId(String itemId) {
        return bidRepository.findByItemId(itemId);
    }


    public List<Bid> getAllBids() {
        return bidRepository.findAll();
    }
}
