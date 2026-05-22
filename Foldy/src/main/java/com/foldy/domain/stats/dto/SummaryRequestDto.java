package com.foldy.domain.stats.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryRequestDto {
    private List<Long> memoIds; // 요약하고 싶은 메모 ID 리스트 (선택적 요약 기능 대응)
}