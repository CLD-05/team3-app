package com.foldy.domain.memo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemoListItemDto {

    private Long idxMemo;
    private String title;
    private String contentPreview;
    private List<TagInfo> tags;
    private LocalDateTime updateDate;

    public record TagInfo(Long idxTag, String name, String color) {}
}
