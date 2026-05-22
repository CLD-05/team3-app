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
    private LocalDateTime createDate;

    public static TagResponse from(TbTag tag) {
        return TagResponse.builder()
                .tagId(tag.getIdxTag())
                .name(tag.getName())
                .color(tag.getColor())
                .createDate(tag.getCreateDate())
                .build();
    }
}
