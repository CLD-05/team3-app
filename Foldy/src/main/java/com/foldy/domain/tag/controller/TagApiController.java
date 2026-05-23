package com.foldy.domain.tag.controller;

import com.foldy.domain.tag.dto.TagRequest;
import com.foldy.domain.tag.dto.TagResponse;
import com.foldy.domain.tag.service.TagService;
import com.foldy.global.controller.BaseController;
import com.foldy.global.exception.CustomException;
import com.foldy.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagApiController extends BaseController {

    private final TagService tagService;

    // ### getCurrentUserIdx() → requireUserIdx() 로 변경
    // ### getCurrentUserIdx() 는 null을 반환할 수 있습니다.
    // ### null인 채로 서비스에 전달되면 NPE가 발생합니다.
    // ### 로그인 여부를 먼저 체크하고 userIdx를 안전하게 가져옵니다.

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        return ok(tagService.getTags(requireUserIdx()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody TagRequest request) {
        return ok(tagService.createTag(requireUserIdx(), request));
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagRequest request) {
        return ok(tagService.updateTag(requireUserIdx(), tagId, request));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(requireUserIdx(), tagId);
        return ok();
    }

    // ### requireUserIdx() 헬퍼
    // ### null이면 즉시 401을 던집니다.
    private Long requireUserIdx() {
        Long userIdx = getCurrentUserIdx();
        if (userIdx == null) throw CustomException.unauthorized("로그인이 필요합니다.");
        return userIdx;
    }
}