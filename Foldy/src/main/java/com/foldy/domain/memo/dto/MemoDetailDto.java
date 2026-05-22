package com.foldy.domain.memo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MemoDetailDto {

    private Long idxMemo;
    private Long idxFolder;
    private String title;
    private String content;
    private List<TagInfo> tags;
    private List<ImageInfo> images;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public record TagInfo(Long idxTag, String name, String color) {}
    public record ImageInfo(Long idxMemoImage, String s3Url, String fileName) {}
}
