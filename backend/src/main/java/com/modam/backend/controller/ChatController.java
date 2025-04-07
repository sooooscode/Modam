package com.modam.backend.controller;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket으로 메시지 수신 → 저장 및 전송
    @MessageMapping("/chat/{clubId}")
    public void sendMessage(@DestinationVariable int clubId, ChatMessageDto message) {
        ChatMessageDto saved = chatService.saveChatMessage(clubId, message);
        messagingTemplate.convertAndSend("/topic/chat/" + clubId, saved);
    }

    // 기존 채팅 내역 조회 (REST API)
    @GetMapping("/history/{clubId}")
    public List<ChatMessageDto> getChatHistory(@PathVariable int clubId) {
        return chatService.getChatHistory(clubId);
    }
}
