package com.example.usedauction.service;

import com.example.usedauction.model.ChatMessage;
import com.example.usedauction.model.ChatRoom;
import com.example.usedauction.repository.ChatMessageRepository;
import com.example.usedauction.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public ChatMessage sendMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    public ChatRoom createChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatMessage> getMessagesForChatRoom(String chatRoomId) {
        return chatMessageRepository.findByChatRoomId(chatRoomId);
    }
    public List<ChatRoom> getChatRooms() {
        return chatRoomRepository.findAll();
    }
}