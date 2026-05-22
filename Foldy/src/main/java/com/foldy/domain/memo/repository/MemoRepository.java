package com.foldy.domain.memo.repository;

import com.foldy.domain.memo.entity.TbMemo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<TbMemo, Long> {

    // 폴더별 메모 목록 (페이징) — 메모 목록 조회 API용
    Page<TbMemo> findByFolder_IdxFolderAndUser_IdxUser(Long folderIdx, Long userIdx, Pageable pageable);

    // 단건 조회 + 소유권 검증 — 상세/수정/삭제 공용
    Optional<TbMemo> findByIdxMemoAndUser_IdxUser(Long idxMemo, Long userIdx);

    // 태그-메모 연결 기능 추가: 태그 삭제 시 해당 사용자의 메모 태그 문자열 정리용
    List<TbMemo> findByUser_IdxUser(Long userIdx);
}
