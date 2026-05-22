package com.foldy.domain.memo.controller;

import com.foldy.domain.memo.dto.MemoCreateDto;
import com.foldy.domain.memo.dto.MemoDetailDto;
import com.foldy.domain.memo.dto.MemoListItemDto;
import com.foldy.domain.memo.dto.MemoTagUpdateDto;
import com.foldy.domain.memo.dto.MemoUpdateDto;
import com.foldy.domain.memo.service.MemoService;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.controller.BaseController;
import com.foldy.global.exception.CustomException;
import com.foldy.global.response.ApiResponse;
import com.foldy.global.response.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 이미지 업로드 — POST /api/memo/{idxMemo}/image (multipart/form-data, field: file)
    @PostMapping("/{idxMemo}/image")
    public ResponseEntity<ApiResponse<MemoDetailDto.ImageInfo>> uploadImage(
            @PathVariable Long idxMemo,
            @RequestParam("file") MultipartFile file) {
        TbUser user = requireUser();
        return ok(memoService.uploadImage(idxMemo, file, user));
    }

    // 이미지 삭제 — DELETE /api/memo/image/{idxMemoImage}
    @DeleteMapping("/image/{idxMemoImage}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long idxMemoImage) {
        TbUser user = requireUser();
        memoService.deleteImage(idxMemoImage, user);
        return ok();
    }

    private TbUser requireUser() {
        TbUser user = getCurrentUser();
        if (user == null) throw CustomException.unauthorized("로그인이 필요합니다.");
        return user;
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<MemoListItemDto>>> searchMemos(
            @RequestParam("keyword") String keyword) {
        TbUser user = requireUser();
        
        // 💡 기존에 이미 작성되어 있던 searchMemosByKeyword를 호출합니다.
        java.util.List<MemoListItemDto> searchResults = memoService.searchMemosByKeyword(user.getIdxUser(), keyword);
        
        return ok(searchResults);
    }
}
