package com.modam.backend.model;

public enum MessageType {
    GREETING,             // 사용자가 입장하며 인사
    TOPIC_START,          // AI 사회자가 발제문(대주제) 발표
    SUBTOPIC,             // 사회자가 선택한 소주제(참여자 의견)
    DISCUSSION,           // 자유로운 채팅 메시지

    RAISE_HAND,           // 사용자가 손들기
    NEXT_TOPIC_VOTE,      // '다음 주제로 넘어가시겠습니까?' 예/아니오 투표
    NEXT_TOPIC_RESULT,    // 투표 결과 알림

    SESSION_END,          // 모임 종료 알림
    SUMMARY               // AI 요약문 전송
}
