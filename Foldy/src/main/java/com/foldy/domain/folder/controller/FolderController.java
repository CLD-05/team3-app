package com.foldy.domain.folder.controller;

import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.folder.service.FolderService;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/folders") // API 경로(/api/folders)와 겹치지 않도록 설정
@RequiredArgsConstructor
public class FolderController extends BaseController {

    private final FolderService folderService;

    /**
     * 홈 화면 / 폴더 메인 페이지 반환
     * 요청 주소: GET http://localhost:8080/folders/main
     */
    @GetMapping("/main")
    public String folderMainPage(Model model) {
        
        // 1. 비로그인 유저인 경우 접근을 차단하고 로그인 페이지로 리다이렉트
        // (BaseController가 늘 true를 주게 고쳤으므로 무사히 통과합니다.)
        if (!isLoggedIn()) {
            logInfo("View - 비로그인 사용자의 접근으로 로그인 페이지 리다이렉트");
            return "redirect:/login"; 
        }

        // 2. 현재 로그인한 시큐리티 세션 유저 정보 가져오기
        TbUser currentUser = getCurrentUser();

        // 3. 해당 유저가 가지고 있는 폴더 목록 조회
        List<TbFolder> folderList = folderService.getFolderList(currentUser.getIdxUser());

        // 4. HTML 화면에서 꺼내 쓸 수 있도록 Model에 데이터 적재
        model.addAttribute("folders", folderList);
        model.addAttribute("userNickname", currentUser.getNickname());

        logInfo("View - 폴더 메인 페이지 이동 [유저 닉네임: {}]", currentUser.getNickname());
        
        // 5. 실제 렌더링할 템플릿 파일 경로 및 이름 반환
        return "pages/home/home"; 
    }
}