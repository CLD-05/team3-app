package com.foldy.domain.stats.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.foldy.domain.stats.Dto.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StatsService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestClient restClient = RestClient.builder().build();

    // 1. Gemini API 연동 메모 요약
    public SummaryResponseDto getAiSummary(SummaryRequestDto request) {
        // TODO: 실제 구현 시에는 request.memoIds()를 이용해 JPA로 메모 본문들을 이어붙여(String) 가져옵니다.
        String combinedMemos = "학습자가 작성한 메모 내용들... (예: 자바 스프링 빈 생명주기, JPA 영속성 컨텍스트 등)";

        // Gemini API 호출을 위한 프롬프트 작성
        String prompt = "다음은 10~20대 학생이 공부하며 작성한 메모 메모들입니다. 핵심 내용을 3줄로 친절하게 요약해 주세요:\n" + combinedMemos;

        // Gemini API Endpoint (2026년 기준 표준 API 포맷)
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiApiKey;

        // Gemini API 스펙에 맞춘 Request Body 구성 (Map 구조 이용)
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        try {
            // 외부 API 동기 호출
            Map<String, Object> apiResponse = restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // API 응답 파싱 (Gemini JSON 계층 구조에서 text 추출)
            // candidates[0].content.parts[0].text
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) apiResponse.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String aiSummaryResult = (String) parts.get(0).get("text");

            return new SummaryResponseDto("success", aiSummaryResult, LocalDateTime.now());

        } catch (Exception e) {
            // API 호출 실패 시 폴백(Fallback) 처리 또는 예외 전환
            return new SummaryResponseDto("error", "AI 요약을 불러오는 중 오류가 발생했습니다.", LocalDateTime.now());
        }
    }

    // 2. 태그별 통계 조회 (JPA Repository 호출 부재로 샘플 데이터 대체)
    public List<TagStatResponseDto> getTagStatistics() {
        // 실제 구현 시: return memoTagRepository.countTagsGroupByTagName();
        return Arrays.asList(
            new TagStatResponseDto("Spring Boot", 24L),
            new TagStatResponseDto("Java 17", 18L),
            new TagStatResponseDto("AWS EC2", 9L)
        );
    }

    // 3. 최근 활동 타임라인 조회
    public List<ActivityResponseDto> getActivityTimeline() {
        // 실제 구현 시: return activityLogRepository.findTop10ByOrderByCreatedAtDesc();
        return Arrays.asList(
            new ActivityResponseDto("WRITE", "스프링 시큐리티 설정 메모를 추가했습니다.", LocalDateTime.now().minusHours(2)),
            new ActivityResponseDto("TAG", "'Infrastructure' 태그를 새로 생성했습니다.", LocalDateTime.now().minusDays(1))
        );
    }
}