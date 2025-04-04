package com.modam.backend.service;

import com.modam.backend.dto.ChatMessageDto;
import com.modam.backend.model.BookClub;
import com.modam.backend.model.ChatMessage;
import com.modam.backend.model.User;
import com.modam.backend.repository.BookClubRepository;
import com.modam.backend.repository.ChatMessageRepository;
import com.modam.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final BookClubRepository bookClubRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageDto saveChatMessage(int clubId, ChatMessageDto dto) {
        BookClub bookClub = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("BookClub not found with id: " + clubId));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));

        ChatMessage chatMessage = new ChatMessage(
                bookClub,
                dto.getUserId(),
                user.getUserName(),
                dto.getContent()
        );

        chatMessageRepository.save(chatMessage);

        return new ChatMessageDto(
                clubId,
                dto.getUserId(),
                user.getUserName(),
                dto.getContent(),
                chatMessage.getCreatedTime()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatHistory(int clubId) {
        return chatMessageRepository.findByBookClubClubId(clubId)
                .stream()
                .map(msg -> new ChatMessageDto(
                        clubId,
                        msg.getUserId(),
                        msg.getUserName(),
                        msg.getContent(),
                        msg.getCreatedTime()
                ))
                .collect(Collectors.toList());
    }
}
