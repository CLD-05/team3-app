package com.foldy.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
		@NotBlank(message = "닉네임은 필수입니다.") @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.") String nickname,

		// ### 추가 — null 허용 (API 키 없이도 프로필 수정 가능)
		String geminiApiKey) {
}