package com.modam.backend.dto;

import com.modam.backend.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private MessageType messageType;
    private int clubId;
    private String userId;
    private String userName;
    private String content;
    private Timestamp createdTime;
}
