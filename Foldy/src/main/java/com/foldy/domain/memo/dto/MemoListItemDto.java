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

	public record TagInfo(Long idxTag, String name, String color) {
	}

	// MemoListItemDto.java
	// ### contentPreview HTML 태그 제거 필요
	// ### memo.getContent() 는 Quill 에디터에서 저장한 HTML입니다.
	// ### "<p>극한의 개념...</p>" 이런 형태라서 그냥 자르면
	// ### 화면에 HTML 태그가 그대로 노출됩니다.
	// ### 태그를 제거하고 순수 텍스트만 추출해야 합니다.
	public static MemoListItemDto from(TbMemo memo) {
		String preview = memo.getContent();
		if (preview != null) {
			// ### HTML 태그 제거 후 미리보기 생성
			// ### replaceAll("<[^>]*>", "") 로 모든 HTML 태그를 제거합니다.
			preview = preview.replaceAll("<[^>]*>", "").trim();
			if (preview.length() > 50) {
				preview = preview.substring(0, 50) + "...";
			}
		}
		return MemoListItemDto.builder().idxMemo(memo.getIdxMemo()).title(memo.getTitle()).contentPreview(preview)
				.updateDate(memo.getUpdateDate()).tags(List.of()).build();
	}
}
