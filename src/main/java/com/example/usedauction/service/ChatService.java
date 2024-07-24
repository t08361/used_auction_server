package com.example.usedauction.service;

import com.example.usedauction.model.ChatMessage;
import com.example.usedauction.model.ChatRoom;
import com.example.usedauction.repository.ChatMessageRepository;
import com.example.usedauction.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public ChatMessage sendMessage(ChatMessage chatMessage) {
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        Date lastMessageTime = Date.from(chatMessage.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
        updateLastMessage(chatMessage.getChatRoomId(), chatMessage.getContent(), lastMessageTime);
        return savedMessage;
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

    public void updateLastMessage(String chatRoomId, String lastMessage, Date lastMessageTime) {
        // chatRoomId에 해당하는 ChatRoom을 데이터베이스에서 찾음
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatRoomId);

        // 해당 ChatRoom이 존재할 경우
        if (optionalChatRoom.isPresent()) {
            // ChatRoom 객체를 가져옴
            ChatRoom chatRoom = optionalChatRoom.get();

            // ChatRoom의 마지막 메시지와 마지막 메시지 시간을 업데이트
            chatRoom.setLastMessage(lastMessage);
            chatRoom.setLastMessageTime(lastMessageTime);

            // 업데이트된 ChatRoom 객체를 데이터베이스에 저장
            chatRoomRepository.save(chatRoom);
        } else {
            throw new RuntimeException("ChatRoom not found");
        }
    }
}