package com.foldy.domain.memo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import com.foldy.domain.memo.entity.TbMemo;

@Getter
@Builder
public class MemoListItemDto {

    private Long idxMemo;
    private String title;
    private String contentPreview;
    private List<TagInfo> tags;
    private LocalDateTime updateDate;

    public record TagInfo(Long idxTag, String name, String color) {}
    
    public static MemoListItemDto from(TbMemo memo) {
        // 본문 내용이 길 경우를 대비해 contentPreview에 들어갈 요약본 처리
        // (내용이 50자보다 길면 잘라서 '...'을 붙이고, 짧으면 그대로 보여줍니다)
        String preview = memo.getContent();
        if (preview != null && preview.length() > 50) {
            preview = preview.substring(0, 50) + "...";
        }

        return MemoListItemDto.builder()
                .idxMemo(memo.getIdxMemo())
                .title(memo.getTitle()) 
                .contentPreview(preview)
                .updateDate(memo.getUpdateDate()) 
                .tags(List.of()) // 태그는 우선 빈 리스트로 안전하게 처리
                .build();
    }
}
