package com.example.usedauction.repository;

import com.example.usedauction.model.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {

    List<Bid> findByItemId(String itemId);
}
