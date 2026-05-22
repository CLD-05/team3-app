package com.foldy.domain.memo.util;

import com.foldy.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class S3Uploader {

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Value("${cloud.aws.s3.bucket:}")
    private String bucket;

    private S3Client s3Client;

    @PostConstruct
    void init() {
        if (accessKey.isBlank() || secretKey.isBlank() || bucket.isBlank()) {
            log.warn("[S3Uploader] S3 자격증명 미설정 — 이미지 업로드 API 호출 시 실패합니다.");
            return;
        }
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    // 파일 업로드 → 퍼블릭 URL 반환
    public Uploaded upload(MultipartFile file, String dir) {
        ensureClient();
        if (file == null || file.isEmpty()) {
            throw CustomException.badRequest("업로드할 파일이 비어있습니다.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) ext = originalName.substring(dot);
        String key = dir + "/" + UUID.randomUUID() + ext;

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException e) {
            log.error("[S3Uploader] 업로드 실패: {}", e.getMessage());
            throw CustomException.badRequest("파일 업로드에 실패했습니다.");
        }

        String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        return new Uploaded(url, originalName);
    }

    // URL 기반 삭제
    public void delete(String url) {
        ensureClient();
        if (url == null || url.isBlank()) return;
        String prefix = "https://" + bucket + ".s3." + region + ".amazonaws.com/";
        if (!url.startsWith(prefix)) return;
        String key = url.substring(prefix.length());
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    private void ensureClient() {
        if (s3Client == null) {
            throw CustomException.badRequest("S3 설정이 누락되어 이미지 기능을 사용할 수 없습니다.");
        }
    }

    public record Uploaded(String url, String originalFileName) {}
}
