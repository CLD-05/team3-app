package com.foldy.domain.memo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MemoUpdateDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 255, message = "255자 이내로 제목을 작성해주세요")
    private String title;

    private String content;

    private List<Long> tagIds;
}
