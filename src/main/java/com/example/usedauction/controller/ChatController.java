package com.example.usedauction.controller;

import com.example.usedauction.model.ChatMessage;
import com.example.usedauction.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.usedauction.model.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/messages/{chatRoomId}")
    public List<ChatMessage> getMessages(@PathVariable String chatRoomId) {
        return chatService.getMessagesForChatRoom(chatRoomId);
    }

    @PostMapping("/createRoom")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoom chatRoom) {
        ChatRoom createdRoom = chatService.createChatRoom(chatRoom);
        return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
    }

    @PostMapping("/sendMessage")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage chatMessage) {
        try {
            chatMessage.setTimestamp(LocalDateTime.now());
            ChatMessage savedMessage = chatService.sendMessage(chatMessage);
            return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}