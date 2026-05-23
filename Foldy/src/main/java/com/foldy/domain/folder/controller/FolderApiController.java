package com.foldy.domain.folder.controller;

import com.foldy.domain.folder.dto.FolderRequest;
import com.foldy.domain.folder.dto.FolderResponse;
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

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createFolder(@RequestBody FolderRequest request) {
        if (!isLoggedIn()) return fail("로그인이 필요한 서비스입니다.");
        TbUser currentUser = getCurrentUser();
        Long folderId = folderService.createFolder(currentUser.getIdxUser(), request.getName());
        logInfo("폴더 생성 [ID: {}]", folderId);
        return ok(folderId);
    }

    // ### List<TbFolder> → List<FolderResponse> 로 변경
    // ### 엔티티를 API 응답으로 직접 반환하면 두 가지 문제가 생깁니다.
    // ### 1. 불필요한 필드(연관관계 등)까지 JSON으로 노출될 수 있습니다.
    // ### 2. JPA 지연로딩(LAZY) 설정 시 직렬화 과정에서 에러가 발생할 수 있습니다.
    // ### 앞으로 API 응답은 반드시 DTO(Response)로 변환해서 반환합니다.
    @GetMapping
    public ResponseEntity<ApiResponse<List<FolderResponse>>> getFolders() {
        if (!isLoggedIn()) return fail("로그인이 필요한 서비스입니다.");
        TbUser currentUser = getCurrentUser();
        List<FolderResponse> folders = folderService.getFolderList(currentUser.getIdxUser());
        return ok(folders);
    }

    // ### try-catch 제거
    // ### Service에서 CustomException을 던지면
    // ### GlobalExceptionHandler가 자동으로 잡아서 fail 응답을 반환합니다.
    // ### 컨트롤러에서 try-catch를 직접 쓰면 GlobalExceptionHandler가 동작하지 않습니다.
    // ### 예외 처리는 GlobalExceptionHandler에 맡기고 컨트롤러는 happy path만 작성합니다.
    @PutMapping("/{folderId}")
    public ResponseEntity<ApiResponse<Void>> updateFolder(
            @PathVariable Long folderId,
            @RequestBody FolderRequest request) {
        if (!isLoggedIn()) return fail("로그인이 필요한 서비스입니다.");
        folderService.updateFolder(folderId, request.getName());
        logInfo("폴더 수정 [ID: {}]", folderId);
        return ok();
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<ApiResponse<Void>> deleteFolder(@PathVariable Long folderId) {
        if (!isLoggedIn()) return fail("로그인이 필요한 서비스입니다.");
        folderService.deleteFolder(folderId);
        logInfo("폴더 삭제 [ID: {}]", folderId);
        return ok();
    }
}