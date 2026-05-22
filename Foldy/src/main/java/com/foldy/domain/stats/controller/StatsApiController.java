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

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardBundle() {
        if (!isLoggedIn()) return fail("로그인이 필요합니다.");
        Long userIdx = getCurrentUserIdx();

        Map<String, Object> totalBundle = new HashMap<>();

        try {
            // [1] 기본 통계
            long totalMemos   = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbMemo WHERE Idx_User = ?", Long.class, userIdx);
            long totalFolders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbFolder WHERE Idx_User = ?", Long.class, userIdx);
            long totalTags    = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tbTag WHERE Idx_User = ?", Long.class, userIdx);

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalMemos", totalMemos);
            summary.put("totalFolders", totalFolders);
            summary.put("totalTags", totalTags);
            totalBundle.put("summary", summary);

            // 이번 주 메모 활동 — CreateDate (ERD 컬럼명)
            List<Integer> weeklyCounts = new ArrayList<>();
            String weeklySql = "SELECT COUNT(*) FROM tbMemo WHERE Idx_User = ? AND DATE(CreateDate) = CURDATE() - INTERVAL ? DAY";
            for (int i = 6; i >= 0; i--) {
                Integer count = jdbcTemplate.queryForObject(weeklySql, Integer.class, userIdx, i);
                weeklyCounts.add(count != null ? count : 0);
            }
            summary.put("weeklyCounts", weeklyCounts);

            // [2] 태그 목록 — memo_tag 조인 테이블이 없으므로 tbTag 단순 조회
            String tagSql = "SELECT Name AS tagName FROM tbTag WHERE Idx_User = ? ORDER BY Name";
            List<Map<String, Object>> tagRows = jdbcTemplate.queryForList(tagSql, userIdx);

            List<String> tagLabels = new ArrayList<>();
            List<Integer> tagCounts = new ArrayList<>();
            for (Map<String, Object> row : tagRows) {
                tagLabels.add(String.valueOf(row.get("tagName")));
                tagCounts.add(0); // 메모-태그 연결 테이블 없어서 0
            }

            Map<String, Object> tagStats = new HashMap<>();
            tagStats.put("labels", tagLabels);
            tagStats.put("counts", tagCounts);
            totalBundle.put("tagStats", tagStats);

            // [3] 최근 활동 타임라인 — tbFolder, Idx_Folder, CreateDate
            String timelineSql = "SELECT m.Title AS title, m.CreateDate AS createdAt, f.Name AS folderName " +
                                 "FROM tbMemo m LEFT JOIN tbFolder f ON m.Idx_Folder = f.Idx_Folder " +
                                 "WHERE m.Idx_User = ? ORDER BY m.CreateDate DESC LIMIT 10";
            List<Map<String, Object>> timelineRows = jdbcTemplate.queryForList(timelineSql, userIdx);

            List<Map<String, Object>> recentActivities = new ArrayList<>();
            for (Map<String, Object> row : timelineRows) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("title",      String.valueOf(row.get("title")));
                activity.put("folderName", row.get("folderName") != null ? String.valueOf(row.get("folderName")) : "미분류");
                activity.put("createdAt",  row.get("createdAt"));   // alias 맞춤
                recentActivities.add(activity);
            }
            totalBundle.put("timeline", recentActivities);

            // [4] AI 요약 — tbMmemo 오타 수정, Idx_User, CreateDate
            String memoSql = "SELECT Content FROM tbMemo WHERE Idx_User = ? AND Content IS NOT NULL ORDER BY CreateDate DESC LIMIT 15";
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