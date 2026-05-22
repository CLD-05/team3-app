package com.foldy.domain.memo.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 태그-메모 연결 기능 추가: 메모에 연결할 태그 ID 목록 전용 요청 DTO
@Getter
@Setter
@NoArgsConstructor
public class MemoTagUpdateDto {

    private List<Long> tagIds;
}
