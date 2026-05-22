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
public class ActivityResponseDto {
    private String type;          // 예: "CREATE_MEMO", "ADD_TAG"
    private String content;       // 활동 설명 요약
    private LocalDateTime createdAt;
}