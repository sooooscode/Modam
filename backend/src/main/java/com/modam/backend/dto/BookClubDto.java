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
public class BookClubDto {

    @Schema(description = "북클럽 고유 ID", example = "9")
    private int club_id;

    @Schema(description = "호스트 유저의 ID", example = "1")
    private String host_id;

    @Schema(description = "호스트 유저 이름", example = "사용자01")
    private String host_user_name;

    @Schema(description = "책 ID", example = "1")
    private String book_id;

    @Schema(description = "북클럽 모임 날짜 및 시간", example = "2025-04-01T00:00:00")
    private LocalDateTime meeting_date;

    @Schema(description = "북클럽 상태 (예: PENDING, ACTIVE, CLOSED 등)", example = "PENDING")
    private String status;

    @Schema(description = "좋아요 수", example = "0")
    private int likes;

    @Schema(description = "북클럽 소개 요약", example = "1984 모임")
    private String summary;

    @Schema(description = "검색 인덱스 키워드", example = "")
    private String search_index;

    @Schema(description = "생성일시", example = "2025-04-10T16:30:36")
    private LocalDateTime created_time;

    @Schema(description = "수정일시", example = "2025-04-10T16:30:36")
    private LocalDateTime updated_time;
}
