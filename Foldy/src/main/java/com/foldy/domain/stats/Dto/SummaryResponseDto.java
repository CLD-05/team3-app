package com.foldy.domain.stats.Dto;

import java.time.LocalDateTime;

public record SummaryResponseDto(
    String status,
    String summary,
    LocalDateTime generatedAt
) {}