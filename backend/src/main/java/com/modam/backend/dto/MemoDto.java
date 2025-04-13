package com.modam.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemoDto {

    @Schema(description = "메모 고유 ID", example = "901")
    private Integer memo_id; // 변경: Long → Integer (DB에 맞춤)

    @Schema(description = "북클럽 ID", example = "9")
    private Integer club_id;

    @Schema(description = "작성자 유저 ID", example = "3") // 예시도 숫자로 변경
    private Integer user_id; // 변경: String → Integer (DB 외래키 타입 일치)

    @Schema(description = "메모 내용", example = "모임 중 핵심 토픽 정리 메모입니다.")
    private String content;

    @Schema(description = "메모 작성 시간", example = "2025-04-10T16:20:00")
    private LocalDateTime created_time;

    @Schema(description = "메모 수정 시간", example = "2025-04-10T16:25:00")
    private LocalDateTime updated_time;

    private Boolean isFinalized = false;
}
