package com.modam.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    private int clubId;
    private String userId;
    private String userName;
    private String content;
    private LocalDateTime createdTime;
}
