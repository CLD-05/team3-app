package com.foldy.domain.memo.util;

import com.foldy.domain.memo.dto.PresignedUrlDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.presigned-url-expiration-minutes}")
    private long expirationMinutes;

    /**
     * 업로드용 Presigned PUT URL 발급
     * 
     * @param dir              버킷 내 경로 prefix (예: "memo/123")
     * @param originalFileName 사용자가 올린 원본 파일명
     * @param contentType      MIME 타입 (예: "image/jpeg")
     */
    public PresignedUrlDto presignUpload(String dir, String originalFileName, String contentType) {
        String key = dir + "/" + UUID.randomUUID() + "_" + sanitize(originalFileName);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(putRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        return new PresignedUrlDto(
                presigned.url().toString(), // 클라이언트가 PUT 할 URL
                key, // 객체 키 (confirm에서 검증용)
                publicUrl // DB 저장용 영구 URL
        );
    }

    public void delete(String url) {
        String key = extractKey(url);
        if (key == null)
            return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String extractKey(String url) {
        if (url == null)
            return null;
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (url.startsWith(prefix))
            return url.substring(prefix.length());
        return null;
    }

    private String sanitize(String name) {
        return name == null ? "file" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}