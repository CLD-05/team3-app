// ### FolderRequest.java — dto 패키지로 분리
// ### static inner class로 컨트롤러 안에 두면
// ### 다른 클래스에서 재사용할 수 없고 컨트롤러가 비대해집니다.
// ### DTO는 반드시 별도 파일로 분리합니다.
package com.foldy.domain.folder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderRequest {

    // ### @NotBlank 추가
    // ### @RequestBody로 받을 때 null이나 빈 문자열이 들어올 수 있습니다.
    // ### @Valid와 함께 사용하면 컨트롤러 진입 전에 자동으로 검증됩니다.
    @NotBlank(message = "폴더 이름은 필수입니다.")
    @Size(max = 100, message = "폴더 이름은 100자 이하여야 합니다.")
    private String name;
}