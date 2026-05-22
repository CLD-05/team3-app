package com.foldy.domain.stats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.foldy.domain.stats.Dto.SummaryRequestDto;
import com.foldy.domain.stats.service.StatsService;
import com.foldy.global.controller.BaseController;

@Controller
@RequestMapping("/stats")
public class StatsController extends BaseController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // 사용자가 http://localhost:8080/stats/dashboard 로 접속했을 때
    @GetMapping("/dashboard")
    public String showStatsDashboard(Model model) {
        model.addAttribute("pageTitle", "나의 학습 종합 대시보드");
        
        // 1. AI 요약 데이터 model에 탑재 (조건 없이 전체 요약하므로 null이나 빈 리스트 전달)
        model.addAttribute("aiSummary", statsService.getAiSummary(new SummaryRequestDto(null)));
        
        // 2. 태그 통계 리스트 model에 탑재
        model.addAttribute("tagStats", statsService.getTagStatistics());
        
        // 3. 최근 활동 타임라인 리스트 model에 탑재
        model.addAttribute("activities", statsService.getActivityTimeline());
        
        // 이 세 가지가 다 담긴 상태로 dashboard.html 뷰로 이동합니다.
        return "stats/dashboard"; 
    }
}