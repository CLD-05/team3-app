package com.foldy.domain.memo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedUrlRequestDto {
    private String fileName;
    private String contentType;
}