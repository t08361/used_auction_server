
package com.example.usedauction.controller;

import com.example.usedauction.model.Bid;
import com.example.usedauction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    @Autowired
    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    @PostMapping
    public ResponseEntity<Bid> placeBid(@RequestBody Bid bid) {
        Bid placedBid = bidService.placeBid(bid.getItemId(), bid.getBidderId(), bid.getBidAmount());
        return new ResponseEntity<>(placedBid, HttpStatus.CREATED);
    }


}