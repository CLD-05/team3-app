package com.foldy.domain.memo.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foldy.domain.memo.dto.MemoCreateDto;
import com.foldy.domain.memo.dto.MemoDetailDto;
import com.foldy.domain.memo.dto.MemoListItemDto;
import com.foldy.domain.memo.dto.MemoTagUpdateDto;
import com.foldy.domain.memo.dto.MemoUpdateDto;
import com.foldy.domain.memo.dto.PresignedUrlDto;
import com.foldy.domain.memo.dto.PresignedUrlRequestDto;
import com.foldy.domain.memo.dto.ImageConfirmDto;
import com.foldy.domain.memo.service.MemoService;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.controller.BaseController;
import com.foldy.global.exception.CustomException;
import com.foldy.global.response.ApiResponse;
import com.foldy.global.response.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/memo")
@RequiredArgsConstructor
public class MemoApiController extends BaseController {

    private final MemoService memoService;

    
    // 메모 목록 조회 — GET /api/memo?folderId={}&page={}
    @GetMapping
    public ResponseEntity<PageResponse<MemoListItemDto>> getMemoList(
            @RequestParam("folderId") Long folderId,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        TbUser user = requireUser();
        Page<MemoListItemDto> result = memoService.getMemoList(folderId, user, getPageable(page));
        return toPageResponse(result);
    }

    // 메모 상세 조회 — GET /api/memo/{idxMemo}
    @GetMapping("/{idxMemo}")
    public ResponseEntity<ApiResponse<MemoDetailDto>> getMemo(@PathVariable Long idxMemo) {
        TbUser user = requireUser();
        return ok(memoService.getMemo(idxMemo, user));
    }

    // 메모 생성 — POST /api/memo
    @PostMapping
    public ResponseEntity<ApiResponse<MemoDetailDto>> createMemo(@Valid @RequestBody MemoCreateDto dto) {
        TbUser user = requireUser();
        return ok(memoService.createMemo(dto, user));
    }

    // 메모 수정 — PUT /api/memo/{idxMemo}
    @PutMapping("/{idxMemo}")
    public ResponseEntity<ApiResponse<MemoDetailDto>> updateMemo(
            @PathVariable Long idxMemo,
            @Valid @RequestBody MemoUpdateDto dto) {
        TbUser user = requireUser();
        return ok(memoService.updateMemo(idxMemo, dto, user));
    }

    // 태그-메모 연결 기능 추가: 메모의 태그 ID 목록만 통째로 수정
    @PatchMapping("/{idxMemo}/tags")
    public ResponseEntity<ApiResponse<MemoDetailDto>> updateMemoTags(
            @PathVariable Long idxMemo,
            @RequestBody MemoTagUpdateDto dto) {
        TbUser user = requireUser();
        return ok(memoService.updateMemoTags(idxMemo, dto, user));
    }

    // 메모 삭제 — DELETE /api/memo/{idxMemo}
    @DeleteMapping("/{idxMemo}")
    public ResponseEntity<ApiResponse<Void>> deleteMemo(@PathVariable Long idxMemo) {
        TbUser user = requireUser();
        memoService.deleteMemo(idxMemo, user);
        return ok();
    }

//    // 이미지 업로드 — POST /api/memo/{idxMemo}/image (multipart/form-data, field: file)
//    @PostMapping("/{idxMemo}/image")
//    public ResponseEntity<ApiResponse<MemoDetailDto.ImageInfo>> uploadImage(
//            @PathVariable Long idxMemo,
//            @RequestParam("file") MultipartFile file) {
//        TbUser user = requireUser();
//        return ok(memoService.uploadImage(idxMemo, file, user));
//    }
    
    // 1단계: Presigned URL 발급 — GET /api/memo/{idxMemo}/image/presigned-url
    @PostMapping("/{idxMemo}/presign")
    public ResponseEntity<ApiResponse<PresignedUrlDto>> getImageUploadUrl(
            @PathVariable Long idxMemo,
            @RequestBody PresignedUrlRequestDto request) {
        TbUser user = requireUser();
        return ok(memoService.presignImageUpload(
            idxMemo, request.getFileName(), request.getContentType(), user));
    }

    // 2단계: 업로드 완료 통보 — POST /api/memo/{idxMemo}/image/confirm
    @PostMapping("/{idxMemo}/image/confirm")
    public ResponseEntity<ApiResponse<MemoDetailDto.ImageInfo>> confirmImageUpload(
            @PathVariable Long idxMemo,
            @Valid @RequestBody ImageConfirmDto dto) {
        TbUser user = requireUser();
        return ok(memoService.confirmImageUpload(idxMemo, dto, user));
    }
    

    // 이미지 삭제 — DELETE /api/memo/image/{idxMemoImage}
    @DeleteMapping("/image/{idxMemoImage}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long idxMemoImage) {
        TbUser user = requireUser();
        memoService.deleteImage(idxMemoImage, user);
        return ok();
    }

    // ### searchMemos 메서드 위치 이동
    // ### 메서드 순서를 CRUD 순서에 맞게 정리합니다.
    // ### GET /api/memo/search?keyword= 는 GET /api/memo/{idxMemo} 와
    // ### 경로가 겹칠 수 있으므로 search를 위에 두는 것이 안전합니다.
    // ### Spring은 구체적인 경로(/search)를 PathVariable({idxMemo})보다
    // ### 우선 매핑하므로 현재는 문제 없지만 명시적으로 위에 두는 게 좋습니다.
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MemoListItemDto>>> searchMemos(
            @RequestParam("keyword") String keyword) {
        TbUser user = requireUser();
        List<MemoListItemDto> results = memoService.searchMemosByKeyword(user.getIdxUser(), keyword);
        return ok(results);
    }
    
    // ### requireUser() 헬퍼 메서드
    // ### isLoggedIn() + getCurrentUser() 를 매번 반복하는 대신
    // ### 하나의 메서드로 묶어서 코드 중복을 줄입니다.
    // ### user가 null이면 즉시 401을 던져서 이후 코드가 실행되지 않습니다.
    // ### 다른 컨트롤러에서도 동일한 패턴을 사용하면 좋습니다.
    private TbUser requireUser() {
        TbUser user = getCurrentUser();
        if (user == null) throw CustomException.unauthorized("로그인이 필요합니다.");
        return user;
    }
}
