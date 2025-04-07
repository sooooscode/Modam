package com.modam.backend.service;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.model.*;
import com.modam.backend.repository.BookClubRepository;
import com.modam.backend.repository.ChatMessageRepository;
import com.modam.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final BookClubRepository bookClubRepository;
    private final UserRepository userRepository;
    private final ChatStateManager chatStateManager;  // 상태 관리 객체 주입

    @Transactional
    public ChatMessageDto saveChatMessage(int clubId, ChatMessageDto dto) {
        // DISCUSSION 메시지는 DB에 저장
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

            // 마지막 메시지 시간 갱신
            chatStateManager.updateLastMessageTime(clubId);

            return new ChatMessageDto(
                    clubId,
                    user.getUserId(),
                    user.getUserName(),
                    dto.getContent(),
                    chatMessage.getCreatedTime(),
                    dto.getMessageType()
            );
        }

        // DISCUSSION 외 메시지 타입 분기 처리
        handleMessageType(clubId, dto);
        return dto;
    }

    // 메시지 타입별 로직
    private void handleMessageType(int clubId, ChatMessageDto dto) {
        switch (dto.getMessageType()) {
            case RAISE_HAND -> chatStateManager.raiseHand(clubId, dto.getUserId());
            case NEXT_TOPIC_VOTE -> {
                VoteStatus vote = "YES".equalsIgnoreCase(dto.getContent()) ? VoteStatus.YES : VoteStatus.NO;
                chatStateManager.setVote(clubId, dto.getUserId(), vote);

                Map<String, VoteStatus> voteMap = chatStateManager.getVoteStatusMap(clubId);
                boolean allVoted = voteMap.values().stream().noneMatch(v -> v == VoteStatus.PENDING);
                if (allVoted) {
                    // 추후 투표 결과 브로드캐스트 처리 예정
                }
            }
            case SESSION_END -> {
                // 추후 요약 처리 로직 추가
                System.out.println("Session ended for club " + clubId);
            }
            default -> {
                // 필요 시 처리
            }
        }
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
