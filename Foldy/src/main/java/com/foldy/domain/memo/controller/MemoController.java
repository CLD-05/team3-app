package com.foldy.domain.memo.controller;

import com.foldy.global.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemoController extends BaseController {

    // ### 메모 목록 페이지
    // ### /memo/list?folderId={id} 로 접근합니다.
    // ### home.html 폴더 카드 클릭 시 이 URL로 이동합니다.
    @GetMapping("/memo/list")
    public String memoListPage(@RequestParam(value = "folderId", required = false) Long folderId) {
        if (!isLoggedIn()) return "redirect:/auth/login";
        return "pages/memo/memoList";
    }

    // ### 메모 상세 페이지
    // ### /memo/detail?memoId={id} 로 접근합니다.
    // ### 메모 카드 클릭 시 이 URL로 이동합니다.
    @GetMapping("/memo/detail")
    public String memoDetailPage(@RequestParam(value = "memoId", required = false) Long memoId) {
        if (!isLoggedIn()) return "redirect:/auth/login";
        return "pages/memo/memoDetail";
    }
}