package com.foldy.domain.stats.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagStatResponseDto {
    private String tagName;
    private Long count;
}