package com.foldy.domain.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TagRequest {

    @NotBlank(message = "태그 이름은 필수입니다.")
    @Size(max = 50, message = "태그 이름은 50자 이하로 입력해주세요.")
    private String name;

    @Size(max = 10, message = "색상 값은 10자 이하로 입력해주세요.")
    private String color;
}
