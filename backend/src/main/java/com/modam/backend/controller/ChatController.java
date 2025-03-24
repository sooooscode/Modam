package com.modam.backend.controller;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.messaging.handler.annotation.DestinationVariable;

import java.util.List;


@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat/{clubId}")
    @SendTo("/topic/chat/{clubId}")
    public ChatMessageDto sendMessage(@DestinationVariable int clubId, ChatMessageDto message) {
        return chatService.saveChatMessage(clubId, message);
    }


    @GetMapping("/history/{clubId}")
    public List<ChatMessageDto> getChatHistory(@PathVariable int clubId) {
        return chatService.getChatHistory(clubId);
    }
}
