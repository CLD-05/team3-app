package com.foldy.domain.user.dto;

import com.foldy.domain.user.entity.TbUser;

import java.time.LocalDateTime;

public record ProfileResponse(Long idxUser, String email, String nickname, String role,
		// ### 추가 — 마이페이지에서 저장된 키 확인용
		String geminiApiKey, LocalDateTime createDate) {
	public static ProfileResponse from(TbUser user) {
		return new ProfileResponse(user.getIdxUser(), user.getEmail(), user.getNickname(), user.getRole().name(),
				user.getGeminiApiKey(), user.getCreateDate());
	}
}