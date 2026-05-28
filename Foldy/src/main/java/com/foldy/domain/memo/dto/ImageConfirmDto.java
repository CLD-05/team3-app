package com.foldy.domain.memo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ImageConfirmDto {
    @NotBlank
    private String key;

    @NotBlank
    private String originalFileName;
}