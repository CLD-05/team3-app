package com.foldy.domain.memo.service;

import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.memo.dto.MemoCreateDto;
import com.foldy.domain.memo.dto.MemoDetailDto;
import com.foldy.domain.memo.dto.MemoListItemDto;
import com.foldy.domain.memo.dto.MemoTagUpdateDto;
import com.foldy.domain.memo.dto.MemoUpdateDto;
import com.foldy.domain.memo.entity.TbMemo;
import com.foldy.domain.memo.entity.TbMemoImage;
import com.foldy.domain.memo.repository.MemoImageRepository;
import com.foldy.domain.memo.repository.MemoRepository;
import com.foldy.domain.memo.util.S3Uploader;
import com.foldy.domain.tag.entity.TbTag;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.global.exception.CustomException;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;
    private final MemoImageRepository memoImageRepository;
    private final EntityManager entityManager;
    private final S3Uploader s3Uploader;

    // ─────────────────────────────────────────
    // 목록 조회
    // ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<MemoListItemDto> getMemoList(Long idxFolder, TbUser user, Pageable pageable) {
        Page<TbMemo> page = memoRepository.findByFolder_IdxFolderAndUser_IdxUser(
                idxFolder, user.getIdxUser(), pageable);

        // 페이지 내 모든 메모에서 등장하는 태그 ID 모아서 한 번에 조회 (N+1 회피)
        List<Long> allTagIds = page.getContent().stream()
                .flatMap(m -> parseTagIds(m.getTags()).stream())
                .distinct()
                .toList();

        Map<Long, TbTag> tagMap = loadTagMap(allTagIds, user.getIdxUser());

        return page.map(memo -> {
            List<MemoListItemDto.TagInfo> tagInfos = parseTagIds(memo.getTags()).stream()
                    .map(tagMap::get)
                    .filter(t -> t != null)
                    .map(t -> new MemoListItemDto.TagInfo(t.getIdxTag(), t.getName(), t.getColor()))
                    .toList();

            return MemoListItemDto.builder()
                    .idxMemo(memo.getIdxMemo())
                    .title(memo.getTitle())
                    .contentPreview(previewOf(memo.getContent()))
                    .tags(tagInfos)
                    .updateDate(memo.getUpdateDate())
                    .build();
        });
    }

    // ─────────────────────────────────────────
    // 상세 조회
    // ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public MemoDetailDto getMemo(Long idxMemo, TbUser user) {
        TbMemo memo = findOwnedMemo(idxMemo, user);
        return toDetail(memo);
    }

    // ─────────────────────────────────────────
    // 생성
    // ─────────────────────────────────────────

    public MemoDetailDto createMemo(MemoCreateDto dto, TbUser user) {
        TbFolder folder = findOwnedFolder(dto.getIdxFolder(), user);

        TbMemo memo = TbMemo.builder()
                .folder(folder)
                .user(user)
                .title(dto.getTitle())
                .content(dto.getContent())
                .tags(joinTagIds(dto.getTagIds()))
                .build();

        memoRepository.save(memo);
        log.info("[MemoService] 메모 생성 — idxMemo={}, user={}", memo.getIdxMemo(), user.getEmail());
        return toDetail(memo);
    }

    // ─────────────────────────────────────────
    // 수정
    // ─────────────────────────────────────────

    public MemoDetailDto updateMemo(Long idxMemo, MemoUpdateDto dto, TbUser user) {
        TbMemo memo = findOwnedMemo(idxMemo, user);
        memo.update(dto.getTitle(), dto.getContent(), joinTagIds(dto.getTagIds()));
        return toDetail(memo);
    }

    // 태그-메모 연결 기능 추가: 메모의 태그 ID 목록만 수정
    public MemoDetailDto updateMemoTags(Long idxMemo, MemoTagUpdateDto dto, TbUser user) {
        TbMemo memo = findOwnedMemo(idxMemo, user);
        List<Long> tagIds = normalizeTagIds(dto == null ? null : dto.getTagIds());
        validateOwnedTags(tagIds, user);
        memo.updateTags(joinTagIds(tagIds));
        return toDetail(memo);
    }

    // ─────────────────────────────────────────
    // 삭제
    // ─────────────────────────────────────────

    public void deleteMemo(Long idxMemo, TbUser user) {
        TbMemo memo = findOwnedMemo(idxMemo, user);
        memoRepository.delete(memo);
        log.info("[MemoService] 메모 삭제 — idxMemo={}, user={}", idxMemo, user.getEmail());
    }

    // ─────────────────────────────────────────
    // 이미지 업로드
    // ─────────────────────────────────────────

    public MemoDetailDto.ImageInfo uploadImage(Long idxMemo, MultipartFile file, TbUser user) {
        TbMemo memo = findOwnedMemo(idxMemo, user);
        S3Uploader.Uploaded uploaded = s3Uploader.upload(file, "memo/" + memo.getIdxMemo());

        TbMemoImage image = TbMemoImage.builder()
                .memo(memo)
                .s3Url(uploaded.url())
                .fileName(uploaded.originalFileName())
                .build();
        memoImageRepository.save(image);

        return new MemoDetailDto.ImageInfo(image.getIdxMemoImage(), image.getS3Url(), image.getFileName());
    }

    // ─────────────────────────────────────────
    // 이미지 삭제
    // ─────────────────────────────────────────

    public void deleteImage(Long idxMemoImage, TbUser user) {
        TbMemoImage image = memoImageRepository
                .findByIdxMemoImageAndMemo_User_IdxUser(idxMemoImage, user.getIdxUser())
                .orElseThrow(() -> CustomException.notFound("이미지를 찾을 수 없습니다."));

        s3Uploader.delete(image.getS3Url());
        memoImageRepository.delete(image);
    }

    // ─────────────────────────────────────────
    // 내부 헬퍼
    // ─────────────────────────────────────────

    private TbMemo findOwnedMemo(Long idxMemo, TbUser user) {
        return memoRepository.findByIdxMemoAndUser_IdxUser(idxMemo, user.getIdxUser())
                .orElseThrow(() -> CustomException.notFound("메모를 찾을 수 없습니다."));
    }

    // 다른 도메인 직접 수정 금지 — EntityManager로 우회 조회
    private TbFolder findOwnedFolder(Long idxFolder, TbUser user) {
        return entityManager.createQuery(
                        "SELECT f FROM TbFolder f WHERE f.idxFolder = :id AND f.user.idxUser = :uid",
                        TbFolder.class)
                .setParameter("id", idxFolder)
                .setParameter("uid", user.getIdxUser())
                .getResultStream().findFirst()
                .orElseThrow(() -> CustomException.notFound("폴더를 찾을 수 없습니다."));
    }

    // 태그-메모 연결 기능 추가: 로그인 사용자의 태그만 조회하도록 제한
    private Map<Long, TbTag> loadTagMap(List<Long> tagIds, Long userIdx) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptyMap();
        List<TbTag> tags = entityManager.createQuery(
                        "SELECT t FROM TbTag t WHERE t.idxTag IN :ids AND t.user.idxUser = :uid", TbTag.class)
                .setParameter("ids", tagIds)
                .setParameter("uid", userIdx)
                .getResultList();
        Map<Long, TbTag> map = new HashMap<>();
        for (TbTag t : tags) map.put(t.getIdxTag(), t);
        return map;
    }

    private List<Long> parseTagIds(String tags) {
        if (tags == null || tags.isBlank()) return Collections.emptyList();
        List<Long> result = new ArrayList<>();
        for (String s : tags.split(",")) {
            String trimmed = s.trim();
            if (trimmed.isEmpty()) continue;
            try {
                result.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    private String joinTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return null;
        return tagIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    // 태그-메모 연결 기능 추가: null/중복/잘못된 ID 제거
    private List<Long> normalizeTagIds(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return Collections.emptyList();
        Set<Long> unique = new LinkedHashSet<>();
        for (Long tagId : tagIds) {
            if (tagId != null && tagId > 0) unique.add(tagId);
        }
        return new ArrayList<>(unique);
    }

    // 태그-메모 연결 기능 추가: 다른 사용자의 태그를 메모에 연결하지 못하도록 검증
    private void validateOwnedTags(List<Long> tagIds, TbUser user) {
        if (tagIds == null || tagIds.isEmpty()) return;
        Long ownedCount = entityManager.createQuery(
                        "SELECT COUNT(t) FROM TbTag t WHERE t.user.idxUser = :uid AND t.idxTag IN :ids",
                        Long.class)
                .setParameter("uid", user.getIdxUser())
                .setParameter("ids", tagIds)
                .getSingleResult();
        if (ownedCount != tagIds.size()) {
            throw CustomException.badRequest("사용할 수 없는 태그가 포함되어 있습니다.");
        }
    }

    private String previewOf(String content) {
        if (content == null) return null;
        String plain = content.replaceAll("<[^>]*>", "").trim();
        return plain.length() <= 100 ? plain : plain.substring(0, 100) + "...";
    }

    private MemoDetailDto toDetail(TbMemo memo) {
        Map<Long, TbTag> tagMap = loadTagMap(parseTagIds(memo.getTags()), memo.getUser().getIdxUser());
        List<MemoDetailDto.TagInfo> tagInfos = parseTagIds(memo.getTags()).stream()
                .map(tagMap::get)
                .filter(t -> t != null)
                .map(t -> new MemoDetailDto.TagInfo(t.getIdxTag(), t.getName(), t.getColor()))
                .toList();

        List<MemoDetailDto.ImageInfo> imageInfos = memo.getImages().stream()
                .map(img -> new MemoDetailDto.ImageInfo(img.getIdxMemoImage(), img.getS3Url(), img.getFileName()))
                .toList();

        return MemoDetailDto.builder()
                .idxMemo(memo.getIdxMemo())
                .idxFolder(memo.getFolder().getIdxFolder())
                .title(memo.getTitle())
                .content(memo.getContent())
                .tags(tagInfos)
                .images(imageInfos)
                .createDate(memo.getCreateDate())
                .updateDate(memo.getUpdateDate())
                .build();
    }
}
