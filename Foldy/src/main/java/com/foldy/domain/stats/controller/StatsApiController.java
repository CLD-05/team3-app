package com.foldy.domain.stats.controller;

import com.foldy.global.controller.BaseController;
import com.foldy.global.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/stats")
public class StatsApiController extends BaseController {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    public StatsApiController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 통계 대시보드 화면에 필요한 모든 데이터(AI요약 + 태그 + 타임라인)를 한눈에 제공
     * GET /api/stats
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardBundle() {
        if (!isLoggedIn()) return fail("로그인이 필요합니다.");
        Long userIdx = getCurrentUserIdx();

        Map<String, Object> totalBundle = new HashMap<>();

        try {
            // [1] API 요약 - 기본 통계
            long totalMemos = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM memo WHERE user_idx = ?", Long.class, userIdx);
            long totalFolders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM folder WHERE user_idx = ?", Long.class, userIdx);
            long totalTags = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tag WHERE user_idx = ?", Long.class, userIdx);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalMemos", totalMemos);
            summary.put("totalFolders", totalFolders);
            summary.put("totalTags", totalTags);
            totalBundle.put("summary", summary);

            // 이번 주 메모 활동
            List<Integer> weeklyCounts = new ArrayList<>();
            String weeklySql = "SELECT COUNT(*) FROM memo WHERE user_idx = ? AND DATE(created_at) = CURDATE() - INTERVAL ? DAY";
            for (int i = 6; i >= 0; i--) {
                Integer count = jdbcTemplate.queryForObject(weeklySql, Integer.class, userIdx, i);
                weeklyCounts.add(count != null ? count : 0);
            }
            summary.put("weeklyCounts", weeklyCounts);

            // [2] 태그 통계
            String tagSql = "SELECT t.name AS tagName, COUNT(mt.memo_id) AS cnt " +
                            "FROM tag t LEFT JOIN memo_tag mt ON t.id = mt.tag_id " +
                            "WHERE t.user_idx = ? GROUP BY t.id, t.name ORDER BY cnt DESC";
            List<Map<String, Object>> tagRows = jdbcTemplate.queryForList(tagSql, userIdx);

            List<String> tagLabels = new ArrayList<>();
            List<Integer> tagCounts = new ArrayList<>();
            for (Map<String, Object> row : tagRows) {
                tagLabels.add(String.valueOf(row.get("tagName")));
                tagCounts.add(Integer.parseInt(String.valueOf(row.get("cnt"))));
            }

            Map<String, Object> tagStats = new HashMap<>();
            tagStats.put("labels", tagLabels);
            tagStats.put("counts", tagCounts);
            totalBundle.put("tagStats", tagStats);

            // [3] 최근 활동 타임라인
            String timelineSql = "SELECT m.title, m.created_at, f.name AS folderName " +
                                 "FROM memo m LEFT JOIN folder f ON m.folder_id = f.id " +
                                 "WHERE m.user_idx = ? ORDER BY m.created_at DESC LIMIT 10";
            List<Map<String, Object>> timelineRows = jdbcTemplate.queryForList(timelineSql, userIdx);

            List<Map<String, Object>> recentActivities = new ArrayList<>();
            for (Map<String, Object> row : timelineRows) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("title", String.valueOf(row.get("title")));
                activity.put("folderName", row.get("folderName") != null ? String.valueOf(row.get("folderName")) : "미분류");
                activity.put("createdAt", row.get("created_at"));
                recentActivities.add(activity);
            }
            totalBundle.put("timeline", recentActivities);

            // [4] AI 요약
            String memoSql = "SELECT content FROM memo WHERE user_idx = ? AND content IS NOT NULL ORDER BY created_at DESC LIMIT 15";
            List<String> memoContents = jdbcTemplate.queryForList(memoSql, String.class, userIdx);

            String aiSummary;
            if (memoContents.isEmpty()) {
                aiSummary = "아직 작성된 메모가 없어서 분석을 진행할 수 없어요. 첫 메모를 남겨 학습 성향을 진단받아 보세요! 📝";
            } else {
                StringBuilder promptBuilder = new StringBuilder();
                promptBuilder.append("너는 1020 학습자를 위한 다정한 AI 학습 멘토야. 아래 나열된 사용자의 최근 메모 텍스트들을 분석해서, ");
                promptBuilder.append("어떤 과목이나 개념을 열심히 공부 중인지 한눈에 요약하고 앞으로의 공부 꿀팁과 따뜻한 격려를 2~3줄로 친근하게 작성해줘.\n\n[메모 내용]\n");
                for (String content : memoContents) {
                    promptBuilder.append("- ").append(content).append("\n");
                }
                aiSummary = callGeminiApi(promptBuilder.toString());
            }
            totalBundle.put("aiSummary", aiSummary);

            logInfo("대시보드 통합 데이터 조회 및 AI 분석 완료 (유저 번호: {})", userIdx);

        } catch (Exception e) {
            logError("대시보드 통합 데이터 추출 실패", e);
            return fail("데이터를 읽어오는 중 에러가 발생했습니다. " + e.getMessage());
        }

        return ok(totalBundle);
    }

    // Gemini API 통신 규격 메서드
    private String callGeminiApi(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;

        Map<String, Object> textMap = new HashMap<>();
        textMap.put("text", prompt);

        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("parts", Collections.singletonList(textMap));

        Map<String, Object> contentsMap = new HashMap<>();
        contentsMap.put("contents", Collections.singletonList(partsMap));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(contentsMap, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            List candidates = (List) response.getBody().get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            
            return (String) part.get("text");
        } catch (Exception e) {
            return "메모 요약 비서가 잠시 휴식 중이에요. 하지만 회원님의 학습 열정은 100% 반영 중입니다!";
        }
    }
}