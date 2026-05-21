package com.foldy.global.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/publishing")
public class PublishingController {

    // ─────────────────────────────────────────
    // 회원 / 인증
    // ─────────────────────────────────────────
	@GetMapping("/login")
	public String login() {
	    return "pages/auth/login";  // pages/ 추가
	}

	@GetMapping("/signup")
	public String signup() {
	    return "pages/auth/signup";
	}

    // ─────────────────────────────────────────
    // 홈 (폴더 목록)
    // ─────────────────────────────────────────
	@GetMapping("/home")
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
    // 마이페이지 (프로필)
    // ─────────────────────────────────────────
    @GetMapping("/mypage")
    public String mypage() {
        return "pages/user/mypage";
    }
}