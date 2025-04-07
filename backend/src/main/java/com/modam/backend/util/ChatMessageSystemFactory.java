package com.modam.backend.util;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.model.MessageType;

import java.sql.Timestamp;

public class ChatMessageSystemFactory {

    public static ChatMessageDto nextTopicVotePrompt(int clubId) {
        return ChatMessageDto.builder()
                .clubId(clubId)
                .userId("system")
                .userName("AI 사회자")
                .content("30초 이상 대화가 없어, 다음 주제로 넘어갈까요? (예/아니오)")
                .createdTime(new Timestamp(System.currentTimeMillis()))
                .messageType(MessageType.NEXT_TOPIC_VOTE)
                .build();
    }

    public static ChatMessageDto topicTimeOver(int clubId) {
        return ChatMessageDto.builder()
                .clubId(clubId)
                .userId("system")
                .userName("AI 사회자")
                .content("시간이 완료되었습니다. 다음 주제로 넘어가겠습니다.")
                .createdTime(new Timestamp(System.currentTimeMillis()))
                .messageType(MessageType.NEXT_TOPIC_RESULT)
                .build();
    }
}
