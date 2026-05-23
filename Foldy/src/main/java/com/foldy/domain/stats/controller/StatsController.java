package com.foldy.domain.stats.controller;

import com.foldy.global.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stats")
// ### @RequiredArgsConstructor 추가
// ### 직접 생성자 작성 대신 Lombok으로 자동 생성합니다.
@RequiredArgsConstructor
public class StatsController extends BaseController {

    // ### StatsService, Model, SummaryRequestDto 전부 제거
    // ### 뷰 컨트롤러는 HTML 경로만 반환합니다.
    // ### 데이터는 stats.html에서 JS fetch로 /api/stats 를 비동기 호출합니다.
    // ### Model에 데이터를 담아 Thymeleaf로 렌더링하는 방식은
    // ### 우리 프로젝트 패턴(JS fetch + API)과 맞지 않습니다.

    // ### /stats/dashboard → / 로 변경
    // ### @RequestMapping("/stats") 가 있으니
    // ### @GetMapping 만 해도 /stats 로 매핑됩니다.
    // ### navbar에서 /stats 로 이동하는데
    // ### /stats/dashboard 로 되어 있으면 404가 발생합니다.
    @GetMapping
    public String statsPage() {
        if (!isLoggedIn()) return "redirect:/auth/login";
        return "pages/stats/stats";
    }
}