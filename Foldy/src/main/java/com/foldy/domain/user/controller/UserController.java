package com.foldy.domain.user.controller;
 
import com.foldy.global.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
 
@Controller
public class UserController extends BaseController {
 
    // ─────────────────────────────────────────
    // 인증 페이지 — 로그인 전 접근
    // ─────────────────────────────────────────
 
    // 이미 로그인 상태면 홈으로 (재로그인 방지)
    @GetMapping("/auth/login")
    public String loginPage() {
        if (isLoggedIn()) return "redirect:/home";
        return "pages/auth/login";
    }
 
    @GetMapping("/auth/signup")
    public String signupPage() {
        if (isLoggedIn()) return "redirect:/home";
        return "pages/auth/signup";
    }
 
    // ─────────────────────────────────────────
    // 회원 페이지 — 로그인 후 접근
    // ─────────────────────────────────────────
 
    // 비로그인 상태면 로그인 페이지로 (JS 가드 대신 서버가 처리)
    @GetMapping("/user/mypage")
    public String mypagePage() {
        if (!isLoggedIn()) return "redirect:/auth/login";
        return "pages/user/mypage";
    }
}