package com.foldy.domain.stats.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SummaryResponseDto {
    private String status;
    private String summary;
    private LocalDateTime generatedAt;
}