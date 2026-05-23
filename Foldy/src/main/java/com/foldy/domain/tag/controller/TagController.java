package com.foldy.domain.tag.controller;

import com.foldy.global.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController extends BaseController {

    // ### 뷰 컨트롤러는 HTML 경로만 반환합니다.
    // ### 데이터는 tag.html에서 JS fetch로 /api/tags 를 비동기 호출합니다.
    @GetMapping
    public String tagPage() {
        if (!isLoggedIn()) return "redirect:/auth/login";
        return "pages/tag/tag";
    }
}