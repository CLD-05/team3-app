package com.foldy.domain.stats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.foldy.domain.stats.dto.SummaryRequestDto;
import com.foldy.domain.stats.service.StatsService;
import com.foldy.global.controller.BaseController;

@Controller
@RequestMapping("/stats")
public class StatsController extends BaseController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    public String showStatsDashboard(Model model) {
        model.addAttribute("pageTitle", "나의 학습 종합 대시보드");

        // 1. AI 요약 데이터 model에 탑재
        SummaryRequestDto summaryRequest = new SummaryRequestDto();
        summaryRequest.setMemoIds(null);
        model.addAttribute("aiSummary", statsService.getAiSummary(summaryRequest));

        // 2. 태그 통계 리스트 model에 탑재
        model.addAttribute("tagStats", statsService.getTagStatistics());

        // 3. 최근 활동 타임라인 리스트 model에 탑재
        model.addAttribute("activities", statsService.getActivityTimeline());

        return "stats/dashboard";
    }
}