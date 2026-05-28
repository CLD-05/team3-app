package com.foldy.domain.memo.repository;

import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.memo.entity.TbMemo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<TbMemo, Long> {

    // 폴더별 메모 목록 (페이징) — 메모 목록 조회 API용
    Page<TbMemo> findByFolder_IdxFolderAndUser_IdxUser(Long folderIdx, Long userIdx, Pageable pageable);

    // 단건 조회 + 소유권 검증 — 상세/수정/삭제 공용
    Optional<TbMemo> findByIdxMemoAndUser_IdxUser(Long idxMemo, Long userIdx);

    // 태그-메모 연결 기능 추가: 태그 삭제 시 해당 사용자의 메모 태그 문자열 정리용
    List<TbMemo> findByUser_IdxUser(Long userIdx);
    
    // 로그인한 유저(idxUser)의 메모 중 content 필드에 키워드가 포함된 것만 조회 (LIKE %keyword%)
    @Query("SELECT m FROM TbMemo m WHERE m.user.idxUser = :idxUser AND m.content LIKE %:keyword%")
    List<TbMemo> searchByUserIdAndContent(@Param("idxUser") Long idxUser, @Param("keyword") String keyword);

    // ### 제목 검색 추가
    // ### content만 검색하면 제목에 키워드가 있는 메모는 검색이 안 됩니다.
    // ### title + content 둘 다 검색하도록 변경합니다.
    @Query("SELECT m FROM TbMemo m WHERE m.user.idxUser = :idxUser AND (m.title LIKE %:keyword% OR m.content LIKE %:keyword%)")
    List<TbMemo> searchByUserIdAndKeyword(@Param("idxUser") Long idxUser, @Param("keyword") String keyword);
    

    // 폴더 ID(Idx_Folder)를 기반으로 속한 메모의 총 개수를 구하는 메서드
    long countByFolderIdxFolder(int idxFolder);
    
    // 폴더 엔티티(folder 필드)를 기반으로 속한 메모의 총 개수를 구하는 메서드
    long countByFolder(TbFolder folder);

}
