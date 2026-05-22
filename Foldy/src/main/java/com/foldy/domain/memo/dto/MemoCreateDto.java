package com.foldy.domain.memo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MemoCreateDto {

    @NotNull(message = "폴더 ID를 선택하세요")
    private Long idxFolder;

    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    private String content;

    private List<Long> tagIds;
}
