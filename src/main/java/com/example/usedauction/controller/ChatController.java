package com.example.usedauction.controller;

import com.example.usedauction.model.ChatMessage;
import com.example.usedauction.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @PostMapping("/messages")
    public ChatMessage sendMessage(@RequestBody ChatMessage chatMessage) {
        chatMessage.setTimestamp(System.currentTimeMillis());
        return chatMessageService.save(chatMessage);
    }

    @GetMapping("/messages/{senderId}/{recipientId}")
    public List<ChatMessage> getMessages(@PathVariable String senderId, @PathVariable String recipientId) {
        return chatMessageService.findBySenderIdAndRecipientId(senderId, recipientId);
    }

    @GetMapping("/messages/{recipientId}")
    public List<ChatMessage> getMessagesForRecipient(@PathVariable String recipientId) {
        return chatMessageService.findByRecipientId(recipientId);
    }
}