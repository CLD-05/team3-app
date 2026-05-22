package com.foldy.domain.user.dto;
 
import com.foldy.domain.user.entity.TbUser;
 
import java.time.LocalDateTime;
 
public record ProfileResponse(
        Long idxUser,
        String email,
        String nickname,
        String role,
        LocalDateTime createDate
) {
    // 비밀번호(pass)는 절대 응답에 포함하지 않음
    public static ProfileResponse from(TbUser user) {
        return new ProfileResponse(
                user.getIdxUser(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getCreateDate()
        );
    }
}