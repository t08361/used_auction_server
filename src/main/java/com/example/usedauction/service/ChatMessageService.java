package com.example.usedauction.service;

import com.example.usedauction.model.ChatMessage;
import com.example.usedauction.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> findBySenderIdAndRecipientId(String senderId, String recipientId) {
        return chatMessageRepository.findBySenderIdAndRecipientId(senderId, recipientId);
    }

    public List<ChatMessage> findByRecipientId(String recipientId) {
        return chatMessageRepository.findByRecipientId(recipientId);
    }
}