package com.foldy.domain.folder.controller;

import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// ### @RequestMapping("/folders") 제거
// ### URL이 /folders/main 이었는데 /home 으로 변경했습니다.
// ### 앞으로 뷰 페이지 URL은 도메인 prefix 없이 기능명으로 바로 매핑하는 방식을 사용합니다.
// ### 예시: /home, /user/mypage, /memo/list 등
@Controller
@RequiredArgsConstructor
public class FolderController extends BaseController {

    // ### FolderService 제거
    // ### 홈 화면은 폴더 데이터를 JS fetch로 비동기 조회하므로
    // ### 서버에서 Model에 데이터를 담아 내려줄 필요가 없습니다.
    // ### 뷰 컨트롤러는 "어떤 HTML을 보여줄지"만 결정합니다.

    // ### /folders/main → /home 으로 변경
    // ### 로그인 후 이동 경로가 /home 이므로 URL을 맞춰줍니다.
    @GetMapping("/home")
    public String homePage(Model model) {

        // ### redirect:/login → redirect:/auth/login 으로 변경
        // ### 우리 프로젝트 로그인 URL은 /auth/login 입니다.
        if (!isLoggedIn()) return "redirect:/auth/login";

        // ### 닉네임은 마이페이지 API(/api/auth/me)에서 JS로 가져오므로 제거해도 됩니다.
        // ### 필요하다면 아래처럼 Model에 담아서 Thymeleaf에서 바로 쓸 수 있습니다.
        TbUser currentUser = getCurrentUser();
        model.addAttribute("userNickname", currentUser.getNickname());

        return "pages/home/home";
    }
}