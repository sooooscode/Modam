package com.modam.backend.service;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.model.*;
import com.modam.backend.repository.BookClubRepository;
import com.modam.backend.repository.ChatMessageRepository;
import com.modam.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final BookClubRepository bookClubRepository;
    private final UserRepository userRepository;

    // 손들기 상태 저장용 Map (임시, 차후 ChatStateManager로 대체 예정)
    private final Map<Integer, Set<String>> raisedHandsMap = new HashMap<>();

    // 투표 상태 저장용 Map
    private final Map<Integer, Map<String, VoteStatus>> voteStatusMap = new HashMap<>();

    @Transactional
    public ChatMessageDto saveChatMessage(int clubId, ChatMessageDto dto) {
        // DISCUSSION 메시지만 DB에 저장
        if (dto.getMessageType() == MessageType.DISCUSSION) {
            BookClub bookClub = bookClubRepository.findById(clubId)
                    .orElseThrow(() -> new RuntimeException("BookClub not found with id: " + clubId));

            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

            ChatMessage chatMessage = ChatMessage.builder()
                    .bookClub(bookClub)
                    .user(user)
                    .content(dto.getContent())
                    .build();

            chatMessageRepository.save(chatMessage);

            return new ChatMessageDto(
                    clubId,
                    user.getUserId(),
                    user.getUserName(),
                    dto.getContent(),
                    chatMessage.getCreatedTime(),
                    dto.getMessageType()
            );
        }

        // DISCUSSION 외의 메시지는 타입별 처리
        handleMessageType(clubId, dto);
        return dto;
    }

    // 메시지 타입별 분기 처리
    private void handleMessageType(int clubId, ChatMessageDto dto) {
        switch (dto.getMessageType()) {
            case RAISE_HAND -> handleRaiseHand(clubId, dto.getUserId());
            case NEXT_TOPIC_VOTE -> handleVote(clubId, dto.getUserId(), dto.getContent());
            case SESSION_END -> handleSessionEnd(clubId);
            default -> {
                // 필요 시 추가 처리
            }
        }
    }

    // 손들기 처리
    private void handleRaiseHand(int clubId, String userId) {
        raisedHandsMap.putIfAbsent(clubId, new HashSet<>());
        raisedHandsMap.get(clubId).add(userId);
    }

    // 투표 처리
    private void handleVote(int clubId, String userId, String voteContent) {
        voteStatusMap.putIfAbsent(clubId, new HashMap<>());
        VoteStatus vote = "YES".equalsIgnoreCase(voteContent) ? VoteStatus.YES : VoteStatus.NO;
        voteStatusMap.get(clubId).put(userId, vote);

        // 모든 참여자가 투표했는지 확인
        Map<String, VoteStatus> votes = voteStatusMap.get(clubId);
        if (votes.values().stream().noneMatch(v -> v == VoteStatus.PENDING)) {
            long yesCount = votes.values().stream().filter(v -> v == VoteStatus.YES).count();
            long noCount = votes.size() - yesCount;

            // 투표 결과에 따른 추가 처리 가능
        }
    }

    // 모임 종료 처리
    private void handleSessionEnd(int clubId) {
        // AI 요약 등 추가 작업 예정
        System.out.println("Session ended for club " + clubId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(int clubId) {
        BookClub bookClub = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("BookClub not found with id: " + clubId));

        return chatMessageRepository.findByBookClubOrderByCreatedTimeAsc(bookClub)
                .stream()
                .map(msg -> new ChatMessageDto(
                        clubId,
                        msg.getUser().getUserId(),
                        msg.getUser().getUserName(),
                        msg.getContent(),
                        msg.getCreatedTime(),
                        MessageType.DISCUSSION
                ))
                .collect(Collectors.toList());
    }
}
