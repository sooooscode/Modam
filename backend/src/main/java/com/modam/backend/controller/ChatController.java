package com.modam.backend.controller;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.model.BookClub;
import com.modam.backend.model.MessageType;
import com.modam.backend.service.BookClubService;
import com.modam.backend.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final BookClubService bookClubService;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate, BookClubService bookClubService) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.bookClubService = bookClubService;
    }

    @MessageMapping("/chat/{clubId}")
    public void sendMessage(@DestinationVariable int clubId, ChatMessageDto message) {
        ChatMessageDto saved = chatService.saveChatMessage(clubId, message);

        messagingTemplate.convertAndSend("/topic/chat/" + clubId, saved);

        if (saved.isShouldTriggerAiIntro()) {
            BookClub bookClub = bookClubService.getBookClub(clubId);
            int bookId = bookClub.getBook().getBookId();

            // AI 토픽 생성
            chatService.sendAiMainTopic(clubId, bookId);

            // AI 인삿말 및 대주제
            messagingTemplate.convertAndSend("/topic/chat/" + clubId,
                    new ChatMessageDto(MessageType.TOPIC_START, clubId, 0, "AI 진행자",
                            "안녕하세요 이번 모임은 책 1984에 대한 내용입니다. 첫번째 주제는 다음과 같습니다.",
                            new Timestamp(System.currentTimeMillis())));

            chatService.getFirstDiscussionTopic(clubId).ifPresent(topic -> {
                messagingTemplate.convertAndSend("/topic/chat/" + clubId,
                        new ChatMessageDto(MessageType.TOPIC_START, clubId, 0, "AI 진행자",
                                "대주제 1: " + topic, new Timestamp(System.currentTimeMillis())));
            });
        }

        if (saved.isShouldTriggerFirstDiscussion()) {
            messagingTemplate.convertAndSend("/topic/chat/" + clubId,
                    new ChatMessageDto(MessageType.TOPIC_START, clubId, 0, "AI 진행자",
                            "그럼 사용자 1의 의견에 대해 이야기해봅시다.", new Timestamp(System.currentTimeMillis())));

            chatService.getFirstUserSubtopic(clubId).ifPresent(content -> {
                messagingTemplate.convertAndSend("/topic/chat/" + clubId,
                        new ChatMessageDto(MessageType.TOPIC_START, clubId, 0, "AI 진행자",
                                "안건: " + content, new Timestamp(System.currentTimeMillis())));
            });
        }
    }

    @GetMapping("/history/{club_id}")
    public List<ChatMessageDto> getChatHistory(@PathVariable("club_id") int club_id) {
        return chatService.getChatHistory(club_id);
    }
}
