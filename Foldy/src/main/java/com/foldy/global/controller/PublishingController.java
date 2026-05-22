package com.foldy.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class PublishingController {

    // ─────────────────────────────────────────
    // 회원 / 인증 (주석 풀고 주소 앞에 /auth 붙임)
    // ─────────────────────────────────────────
//    @GetMapping("/auth/login") // ⭐️ 진짜 로그인 화면 주소!
//    public String login() {
//        return "pages/auth/login";  
//    }
//
//    @GetMapping("/auth/signup") // ⭐️ 진짜 회원가입 화면 주소!
//    public String signup() {
//        return "pages/auth/signup";
//    }

    // ─────────────────────────────────────────
    // 홈 (폴더 목록 - 주석 해제)
    // ─────────────────────────────────────────
    @GetMapping("/home") // ⭐️ 진짜 홈 화면 주소!
    public String home() {
        return "pages/home/home";
    }

    // ─────────────────────────────────────────
    // 태그
    // ─────────────────────────────────────────
    @GetMapping("/tag")
    public String tag() {
        return "pages/tag/tag";
    }

    // ─────────────────────────────────────────
    // 메모
    // ─────────────────────────────────────────
    @GetMapping("/memo/list")
    public String memoList() {
        return "pages/memo/memoList";
    }

    @GetMapping("/memo/detail")
    public String memoDetail() {
        return "pages/memo/memoDetail";
    }

    // ─────────────────────────────────────────
    // 통계
    // ─────────────────────────────────────────
    @GetMapping("/stats")
    public String stats() {
        return "pages/stats/stats";
    }

    // ─────────────────────────────────────────
    // 마이페이지 (시큐리티 통과를 위해 앞에 /user 붙임)
    // ─────────────────────────────────────────
//    @GetMapping("/user/mypage") // ⭐️ 진짜 마이페이지 화면 주소!
//    public String mypage() {
//        return "pages/user/mypage";
//    }
}