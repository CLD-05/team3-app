package com.foldy.domain.stats.service;

import com.foldy.domain.memo.entity.TbMemo;
import com.foldy.domain.stats.repository.StatsRepository;
import com.foldy.domain.tag.entity.TbTag;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.domain.user.repository.UserRepository;
import com.foldy.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    // ### gemini.api.key @Value 제거
    // ### 유저별 API 키를 사용하므로 application.properties에 키가 없어도 됩니다.
    // ### 기존 StatsService의 하드코딩 더미 데이터 전부 제거

    private final StatsRepository statsRepository;
    private final UserRepository userRepository;

    // ### RestTemplate 직접 생성
    // ### Stats에서만 쓰는 간단한 외부 API 호출이라 Bean 등록 없이 사용합니다.
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getDashboardBundle(Long userIdx) {
        TbUser user = userRepository.findById(userIdx)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        Map<String, Object> result = new HashMap<>();

        // ─────────────────────────────────────────
        // [1] 기본 통계
        // ─────────────────────────────────────────
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalMemos",   statsRepository.countMemos(userIdx));
        summary.put("totalFolders", statsRepository.countFolders(userIdx));
        summary.put("totalTags",    statsRepository.countTags(userIdx));

        // ─────────────────────────────────────────
        // [2] 이번 주 메모 활동 (7일)
        // ─────────────────────────────────────────
        List<Integer> weeklyCounts = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime start = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime end   = start.plusDays(1);
            weeklyCounts.add((int) statsRepository.countMemosBetween(userIdx, start, end));
        }
        summary.put("weeklyCounts", weeklyCounts);
        result.put("summary", summary);

        // ─────────────────────────────────────────
        // [3] 태그별 메모 개수
        // ### tags 컬럼이 "1,2,3" 문자열이라
        // ### 각 태그 ID가 포함된 메모 수를 stream으로 계산합니다.
        // ─────────────────────────────────────────
        List<TbTag>  tags      = statsRepository.findTagsOrderByName(userIdx);
        List<TbMemo> allMemos  = statsRepository.findAllMemos(userIdx);

        List<String>  tagLabels = new ArrayList<>();
        List<Integer> tagCounts = new ArrayList<>();
        List<String>  tagColors = new ArrayList<>();

        for (TbTag tag : tags) {
            tagLabels.add(tag.getName());
            tagColors.add(tag.getColor());
            long count = allMemos.stream()
                    .filter(m -> m.getTags() != null &&
                            Arrays.asList(m.getTags().split(","))
                                  .contains(String.valueOf(tag.getIdxTag())))
                    .count();
            tagCounts.add((int) count);
        }

        Map<String, Object> tagStats = new HashMap<>();
        tagStats.put("labels", tagLabels);
        tagStats.put("counts", tagCounts);
        tagStats.put("colors", tagColors);
        result.put("tagStats", tagStats);

        // ─────────────────────────────────────────
        // [4] 최근 활동 타임라인
        // ─────────────────────────────────────────
        List<Map<String, Object>> timeline = statsRepository.findRecentMemos(userIdx)
                .stream()
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("title",      m.getTitle());
                    item.put("folderName", m.getFolder() != null
                            ? m.getFolder().getName() : "미분류");
                    item.put("createdAt",  m.getCreateDate());
                    return item;
                })
                .collect(Collectors.toList());
        result.put("timeline", timeline);

        // ─────────────────────────────────────────
        // [5] AI 요약
        // ### 유저의 Gemini API 키가 있을 때만 호출합니다.
        // ### 키가 없으면 null 반환 → 프론트에서 등록 안내를 보여줍니다.
        // ─────────────────────────────────────────
        String aiSummary = null;
        if (user.getGeminiApiKey() != null && !user.getGeminiApiKey().isBlank()) {
            List<String> recentContents = statsRepository.findRecentContents(userIdx)
                    .stream()
                    .map(c -> c.replaceAll("<[^>]*>", "").trim())
                    .filter(c -> !c.isBlank())
                    .collect(Collectors.toList());

            aiSummary = recentContents.isEmpty()
                    ? "아직 작성된 메모가 없어요. 첫 메모를 남겨보세요! 📝"
                    : callGeminiApi(user.getGeminiApiKey(), recentContents);
        }
        result.put("aiSummary", aiSummary);

        log.info("[StatsService] 대시보드 조회 완료 — userIdx={}", userIdx);
        return result;
    }

    // ─────────────────────────────────────────
    // Gemini API 호출
    // ### 실패 시 예외 대신 안내 메시지 반환
    // ### API 오류로 페이지 전체가 깨지는 걸 방지합니다.
    // ─────────────────────────────────────────
    private String callGeminiApi(String apiKey, List<String> contents) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        String prompt = "너는 10~20대 학습자를 위한 다정한 AI 학습 멘토야. "
                + "아래 메모들을 분석해서 어떤 내용을 공부 중인지 요약하고 "
                + "따뜻한 격려를 2~3줄로 친근하게 작성해줘.\n\n[메모 내용]\n"
                + contents.stream().map(c -> "- " + c).collect(Collectors.joining("\n"));

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.warn("[StatsService] Gemini API 호출 실패: {}", e.getMessage());
            return "AI 요약을 불러오는 중 오류가 발생했습니다. API 키를 확인해주세요.";
        }
    }
}