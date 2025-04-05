package com.modam.backend.dto;

import com.modam.backend.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    private MessageType messageType;  // 메시지 유형 추가

    private int clubId;
    private String userId;
    private String userName;
    private String content;
    private Timestamp createdTime;
}
