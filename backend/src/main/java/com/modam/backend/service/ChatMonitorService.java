package com.modam.backend.service;

import com.modam.backend.util.ChatMessageSystemFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatMonitorService {

    private final ChatStateManager chatStateManager;
    private final SimpMessagingTemplate messagingTemplate;

    // 5초마다 상태 확인
    @Scheduled(fixedRate = 5000)
    public void checkInactivityAndTimeLimit() {
        Set<Integer> clubIds = chatStateManager.getAllClubIds();

        for (int clubId : clubIds) {
            long now = System.currentTimeMillis();

            // 1. 30초 이상 채팅이 없었는가?
            long lastMessageTime = chatStateManager.getLastMessageTime(clubId);
            if (now - lastMessageTime > 30_000) {
                messagingTemplate.convertAndSend("/topic/chat/" + clubId,
                        ChatMessageSystemFactory.nextTopicVotePrompt(clubId));
            }

            // 2. 현재 주제가 10분 이상 진행됐는가?
            long topicStartTime = chatStateManager.getTopicStartTime(clubId);
            if (now - topicStartTime > 600_000) {
                messagingTemplate.convertAndSend("/topic/chat/" + clubId,
                        ChatMessageSystemFactory.topicTimeOver(clubId));
            }
        }
    }
}
