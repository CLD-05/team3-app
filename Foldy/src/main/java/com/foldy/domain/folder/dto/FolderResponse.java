// ### FolderResponse.java — 새로 추가
// ### 엔티티 대신 API 응답 전용 DTO를 만들어 필요한 필드만 노출합니다.
// ### record를 사용하면 getter/생성자/equals/hashCode를 자동으로 만들어줍니다.
package com.foldy.domain.folder.dto;

import com.foldy.domain.folder.entity.TbFolder;
import java.time.LocalDateTime;

public record FolderResponse(
        Long idxFolder,
        String name,
        LocalDateTime createDate
) {
    // ### 정적 팩토리 메서드 패턴
    // ### 엔티티 → DTO 변환 로직을 DTO 안에 두면
    // ### Service에서 .map(FolderResponse::from) 한 줄로 변환할 수 있습니다.
    public static FolderResponse from(TbFolder folder) {
        return new FolderResponse(
                folder.getIdxFolder(),
                folder.getName(),
                folder.getCreateDate()
        );
    }
}