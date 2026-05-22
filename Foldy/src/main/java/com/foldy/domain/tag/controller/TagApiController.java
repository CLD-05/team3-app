package com.foldy.domain.tag.controller;

import com.foldy.domain.tag.dto.TagRequest;
import com.foldy.domain.tag.dto.TagResponse;
import com.foldy.domain.tag.service.TagService;
import com.foldy.global.controller.BaseController;
import com.foldy.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagApiController extends BaseController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        return ok(tagService.getTags(getCurrentUserIdx()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody TagRequest request) {
        return ok(tagService.createTag(getCurrentUserIdx(), request));
    }

    @PatchMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagRequest request) {
        return ok(tagService.updateTag(getCurrentUserIdx(), tagId, request));
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(getCurrentUserIdx(), tagId);
        return ok();
    }
}
