package com.foldy.domain.memo.repository;

import com.foldy.domain.memo.entity.TbMemoImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemoImageRepository extends JpaRepository<TbMemoImage, Long> {

    // 본인 메모의 이미지인지 확인하며 단건 조회 — 이미지 삭제 시 소유권 검증
    Optional<TbMemoImage> findByIdxMemoImageAndMemo_User_IdxUser(Long idxMemoImage, Long userIdx);
}
