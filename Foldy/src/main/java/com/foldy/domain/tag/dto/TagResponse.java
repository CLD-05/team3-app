package com.foldy.domain.tag.dto;

import com.foldy.domain.tag.entity.TbTag;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {

    private Long tagId;
    private String name;
    private String color;
    private long memoCount;
    private LocalDateTime createDate;

    public static TagResponse from(TbTag tag) {
        return TagResponse.builder()
                .tagId(tag.getIdxTag())
                .name(tag.getName())
                .color(tag.getColor())
                .createDate(tag.getCreateDate())
                .build();
    }

    // 태그-메모 연결 기능 추가: 태그별 연결된 메모 개수 포함 응답
    public static TagResponse from(TbTag tag, long memoCount) {
        return TagResponse.builder()
                .tagId(tag.getIdxTag())
                .name(tag.getName())
                .color(tag.getColor())
                .memoCount(memoCount)
                .createDate(tag.getCreateDate())
                .build();
    }
}
