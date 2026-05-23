package com.foldy.domain.stats.repository;

import com.foldy.domain.memo.entity.TbMemo;
import com.foldy.domain.tag.entity.TbTag;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatsRepository {

    private final EntityManager em;

    // ### 전체 메모 개수
    public long countMemos(Long userIdx) {
        return em.createQuery(
                "SELECT COUNT(m) FROM TbMemo m WHERE m.user.idxUser = :uid", Long.class)
                .setParameter("uid", userIdx)
                .getSingleResult();
    }

    // ### 전체 폴더 개수
    public long countFolders(Long userIdx) {
        return em.createQuery(
                "SELECT COUNT(f) FROM TbFolder f WHERE f.user.idxUser = :uid", Long.class)
                .setParameter("uid", userIdx)
                .getSingleResult();
    }

    // ### 전체 태그 개수
    public long countTags(Long userIdx) {
        return em.createQuery(
                "SELECT COUNT(t) FROM TbTag t WHERE t.user.idxUser = :uid", Long.class)
                .setParameter("uid", userIdx)
                .getSingleResult();
    }

    // ### 기간별 메모 개수 — 이번 주 활동 차트용
    public long countMemosBetween(Long userIdx, LocalDateTime start, LocalDateTime end) {
        return em.createQuery(
                "SELECT COUNT(m) FROM TbMemo m " +
                "WHERE m.user.idxUser = :uid " +
                "AND m.createDate >= :start AND m.createDate < :end", Long.class)
                .setParameter("uid", userIdx)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    // ### 유저 전체 메모 — 태그별 카운트용
    public List<TbMemo> findAllMemos(Long userIdx) {
        return em.createQuery(
                "SELECT m FROM TbMemo m WHERE m.user.idxUser = :uid", TbMemo.class)
                .setParameter("uid", userIdx)
                .getResultList();
    }

    // ### 유저 태그 목록 이름순 — 태그 통계용
    public List<TbTag> findTagsOrderByName(Long userIdx) {
        return em.createQuery(
                "SELECT t FROM TbTag t WHERE t.user.idxUser = :uid ORDER BY t.name ASC", TbTag.class)
                .setParameter("uid", userIdx)
                .getResultList();
    }

    // ### 최근 메모 10개 — 타임라인용
    public List<TbMemo> findRecentMemos(Long userIdx) {
        return em.createQuery(
                "SELECT m FROM TbMemo m " +
                "WHERE m.user.idxUser = :uid " +
                "ORDER BY m.createDate DESC", TbMemo.class)
                .setParameter("uid", userIdx)
                .setMaxResults(10)
                .getResultList();
    }

    // ### AI 요약용 최근 메모 내용
    public List<String> findRecentContents(Long userIdx) {
        return em.createQuery(
                "SELECT m.content FROM TbMemo m " +
                "WHERE m.user.idxUser = :uid AND m.content IS NOT NULL " +
                "ORDER BY m.createDate DESC", String.class)
                .setParameter("uid", userIdx)
                .setMaxResults(10)
                .getResultList();
    }
}