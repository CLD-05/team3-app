package com.foldy.domain.stats.Dto;

import java.util.List;

public record SummaryRequestDto(
    List<Long> memoIds // 요약하고 싶은 메모 ID 리스트 (선택적 요약 기능 대응)
) {}
