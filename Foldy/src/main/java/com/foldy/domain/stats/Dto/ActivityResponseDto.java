package com.foldy.domain.stats.Dto;

import java.time.LocalDateTime;

public record ActivityResponseDto(
    String type,        // 예: "CREATE_MEMO", "ADD_TAG"
    String content,     // 활동 설명 요약
    LocalDateTime createdAt
) {}