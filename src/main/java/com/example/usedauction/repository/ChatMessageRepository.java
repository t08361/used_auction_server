package com.example.usedauction.repository;

import com.example.usedauction.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomId(String chatRoomId);
}