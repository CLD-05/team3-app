package com.foldy.domain.stats.controller;

import com.foldy.domain.stats.service.StatsService;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.controller.BaseController;
import com.foldy.global.exception.CustomException;
import com.foldy.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
// ### @RequiredArgsConstructor 추가
// ### 직접 생성자 작성 대신 Lombok으로 자동 생성합니다.
@RequiredArgsConstructor
public class StatsApiController extends BaseController {

    // ### JdbcTemplate, RestTemplate, gemini.api.key 전부 제거
    // ### Controller는 요청/응답만 처리합니다.
    // ### 비즈니스 로직(통계 조회, AI 호출)은 StatsService로 이동했습니다.
    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardBundle() {
        // ### isLoggedIn() → getCurrentUser() 방식으로 변경
        // ### requireUser 패턴으로 통일합니다.
        TbUser user = getCurrentUser();
        if (user == null) throw CustomException.unauthorized("로그인이 필요합니다.");
        return ok(statsService.getDashboardBundle(user.getIdxUser()));
    }
}