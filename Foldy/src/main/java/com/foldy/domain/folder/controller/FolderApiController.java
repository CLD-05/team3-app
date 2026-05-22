package com.foldy.domain.folder.controller;

import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.folder.service.FolderService;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.controller.BaseController;
import com.foldy.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderApiController extends BaseController {

    private final FolderService folderService;

    /**
     * 1. 폴더 생성 API
     * 요청: POST /api/folders {"name": "새로운 폴더명"}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createFolder(@RequestBody FolderRequest request) {
        // BaseController의 세션 로그인 체크 기능 활용
        if (!isLoggedIn()) {
            return fail("로그인이 필요한 서비스입니다.");
        }

        // BaseController의 getCurrentUser()를 통해 시큐리티 인증 유저 엔티티 획득
        TbUser currentUser = getCurrentUser();
        
        // 엔티티 구조에 맞춰 id 혹은 idxUser를 추출하여 서비스로 전달
        Long folderId = folderService.createFolder(currentUser.getIdxUser(), request.getName());
        
        logInfo("API - 폴더 생성 성공 [폴더 ID: {}]", folderId);
        return ok(folderId); // BaseController의 데이터 포함 성공 응답 구조(ok) 사용
    }

    /**
     * 2. 폴더 목록 조회 API (비동기 데이터 전송용)
     * 요청: GET /api/folders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TbFolder>>> getFolders() {
        if (!isLoggedIn()) {
            return fail("로그인이 필요한 서비스입니다.");
        }

        TbUser currentUser = getCurrentUser();
        List<TbFolder> folders = folderService.getFolderList(currentUser.getIdxUser());
        
        return ok(folders);
    }

    /**
     * 3. 폴더 수정 API
     * 요청: PUT /api/folders/{folderId} {"name": "바꿀 폴더명"}
     */
    @PutMapping("/{folderId}")
    public ResponseEntity<ApiResponse<Void>> updateFolder(
            @PathVariable("folderId") Long folderId,
            @RequestBody FolderRequest request) {
        
        if (!isLoggedIn()) {
            return fail("로그인이 필요한 서비스입니다.");
        }

        try {
            folderService.updateFolder(folderId, request.getName());
            logInfo("API - 폴더 수정 성공 [폴더 ID: {}, 변경된 이름: {}]", folderId, request.getName());
            return ok(); // BaseController의 데이터 없는 공통 성공 응답 사용
        } catch (IllegalArgumentException e) {
            logError("API - 폴더 수정 실패", e);
            return fail(e.getMessage()); // 에러 발생 시 공통 실패 응답 반환
        }
    }

    /**
     * 4. 폴더 삭제 API
     * 요청: DELETE /api/folders/{folderId}
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(@PathVariable("folderId") Long folderId) {
        if (!isLoggedIn()) {
            return fail("로그인이 필요한 Service입니다.");
        }

        try {
            folderService.deleteFolder(folderId);
            logInfo("API - 폴더 삭제 성공 [폴더 ID: {}]", folderId);
            return ok();
        } catch (IllegalArgumentException e) {
            logError("API - 폴더 삭제 실패", e);
            return fail(e.getMessage());
        }
    }

    // JSON 요청 데이터를 바인딩하기 위한 DTO 클래스
    @lombok.Getter
    @lombok.Setter
    public static class FolderRequest {
        private String name;
    }
}

