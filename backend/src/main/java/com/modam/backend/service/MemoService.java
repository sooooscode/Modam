package com.modam.backend.service;

import com.modam.backend.dto.MemoDto;
import com.modam.backend.model.Memo;
import com.modam.backend.repository.MemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;

    public MemoDto getMemo(Integer clubId, Integer userId) {
        Memo memo = memoRepository.findByClubIdAndUserId(clubId, userId)
                .orElse(new Memo(null, userId, clubId, "", LocalDateTime.now(), LocalDateTime.now(), false));
        return convertToDto(memo);
    }

    public MemoDto saveOrUpdateMemo(Integer clubId, Integer userId, String content) {
        Memo memo = memoRepository.findByClubIdAndUserId(clubId, userId)
                .orElse(new Memo(null, userId, clubId, "", LocalDateTime.now(), LocalDateTime.now(), false));

        if (Boolean.TRUE.equals(memo.getIsFinalized())) {
            throw new IllegalStateException("이미 확정된 메모는 수정할 수 없습니다.");
        }

        memo.setContent(content);
        memo.setUpdated_time(LocalDateTime.now());
        Memo saved = memoRepository.save(memo);
        return convertToDto(saved);
    }

    public void finalizeMemo(Integer clubId, Integer userId) {
        Memo memo = memoRepository.findByClubIdAndUserId(clubId, userId)
                .orElseThrow(() -> new IllegalArgumentException("메모가 존재하지 않습니다."));

        memo.setIsFinalized(true);
        memo.setUpdated_time(LocalDateTime.now());
        memoRepository.save(memo);
    }

    private MemoDto convertToDto(Memo memo) {
        return new MemoDto(
                memo.getMemo_id(),
                memo.getClub_id(),
                memo.getUser_id(),
                memo.getContent(),
                memo.getCreated_time(),
                memo.getUpdated_time(),
                memo.getIsFinalized()
        );
    }
}
