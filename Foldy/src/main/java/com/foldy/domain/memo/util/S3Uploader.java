package com.foldy.domain.memo.util;

import com.foldy.global.exception.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

// S3 의존성/키 세팅 전까지의 임시 스텁.
// AWS SDK 의존성 추가 + 환경변수 세팅 후 실제 업로드 로직으로 교체 예정.
@Component
public class S3Uploader {

    public Uploaded upload(MultipartFile file, String dir) {
        throw CustomException.badRequest("S3 설정이 누락되어 이미지 업로드를 사용할 수 없습니다.");
    }

    public void delete(String url) {
        // S3 미연결 상태에서는 no-op
    }

    public record Uploaded(String url, String originalFileName) {}
}
