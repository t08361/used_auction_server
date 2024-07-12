
package com.example.usedauction.controller;

import com.example.usedauction.model.Bid;
import com.example.usedauction.service.BidService;
import com.example.usedauction.service.BidService.BidWithNickname;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    @Autowired
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    // 새로운 입찰 정보를 저장하는 엔드포인트
    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Bid> placeBid(@RequestBody Bid bid) {
        Bid placedBid = bidService.placeBid(bid.getItemId(), bid.getBidderId(), bid.getBidAmount());
        return new ResponseEntity<>(placedBid, HttpStatus.CREATED);
    }

    // 모든 입찰 기록을 가져오는 엔드포인트
    @GetMapping
    public ResponseEntity<List<Bid>> getAllBids() {
        List<Bid> bids = bidService.getAllBids();
        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    // 특정 상품의 입찰 기록을 가져오는 엔드포인트
    @GetMapping("/{itemId}")
    public ResponseEntity<List<BidWithNickname>> getBidsByItemId(@PathVariable String itemId) {
        List<BidWithNickname> bids = bidService.getBidsByItemId(itemId);
        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

}
