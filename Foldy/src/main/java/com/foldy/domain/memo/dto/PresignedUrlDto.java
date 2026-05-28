package com.foldy.domain.memo.dto;

public record PresignedUrlDto(
        String uploadUrl,   // 클라이언트가 PUT 할 임시 URL
        String key,         // S3 객체 키
        String publicUrl    // DB 저장용 영구 URL
) {}