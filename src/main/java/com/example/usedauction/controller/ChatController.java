package com.example.usedauction.controller;

import com.example.usedauction.model.ChatMessage;
import com.example.usedauction.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.usedauction.model.ChatRoom;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    // 현재 채팅방의 마지막 메시지 업데이트
    @PostMapping("/updateLastMessage")
    public ResponseEntity<Void> updateLastMessage(@RequestBody Map<String, Object> updateData) {
        try {
            // chatRoomId, lastMessage, lastMessageTime을 추출
            String chatRoomId = (String) updateData.get("chatRoomId");
            String lastMessage = (String) updateData.get("lastMessage");
            String lastMessageTimeString = (String) updateData.get("lastMessageTime");

            // DateTimeFormatter를 사용하여 날짜 문자열을 LocalDateTime 객체로 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime lastMessageTime = LocalDateTime.parse(lastMessageTimeString, formatter);

            // LocalDateTime을 Date로 변환
            Date lastMessageDate = Date.from(lastMessageTime.atZone(ZoneId.systemDefault()).toInstant());

            // ChatService의 updateLastMessage 메서드를 호출하여 채팅방의 마지막 메시지와 시간을 업데이트
            chatService.updateLastMessage(chatRoomId, lastMessage, lastMessageDate);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/chatRooms")
    public ResponseEntity<List<ChatRoom>> getChatRooms() {
        return ResponseEntity.ok(chatService.getChatRooms());
    }
}