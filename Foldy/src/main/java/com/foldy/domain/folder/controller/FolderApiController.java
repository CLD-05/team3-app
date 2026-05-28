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
        // 1. 로그인 여부 체킹 (기존 로직 유지)
        if (!isLoggedIn()) return fail("로그인이 필요한 서비스입니다.");
        
        // 2. 서비스 실행 후 삭제 성공 여부(true/false)를 받습니다.
        boolean isDeleted = folderService.deleteFolder(folderId);
        
        // 3. 성공적으로 삭제된 경우
        if (isDeleted) {
            logInfo("폴더 삭제 성공 [ID: {}]", folderId);
            return ok(); // 원래대로 성공 응답(res.result가 true로 들어감)
        } 
        // 4. 🎯 안에 메모가 있어서 삭제가 거부된 경우 (추가)
        else {
            logInfo("폴더 삭제 거부 (메모 존재) [ID: {}]", folderId);
            // 프론트엔드의 res.message로 전달될 경고 문구를 fail() 안에 담아 리턴합니다.
            return fail("폴더 안에 메모가 존재하여 삭제할 수 없습니다. 메모를 먼저 삭제하거나 이동해 주세요.");
        }
    }
}