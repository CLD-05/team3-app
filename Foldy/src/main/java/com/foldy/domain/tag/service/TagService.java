package com.foldy.domain.tag.service;

import com.foldy.domain.tag.dto.TagRequest;
import com.foldy.domain.tag.dto.TagResponse;
import com.foldy.domain.tag.entity.TbTag;
import com.foldy.domain.tag.repository.TagRepository;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.domain.user.repository.UserRepository;
import com.foldy.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private static final String DEFAULT_COLOR = "#4ECDC4";

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public List<TagResponse> getTags(Long userIdx) {
        if (userIdx == null) {
            throw CustomException.unauthorized("로그인이 필요합니다.");
        }

        return tagRepository.findByUserIdxUserOrderByCreateDateDesc(userIdx)
                .stream()
                .map(TagResponse::from)
                .toList();
    }

    @Transactional
    public TagResponse createTag(Long userIdx, TagRequest request) {
        validateUserIdx(userIdx);
        String name = normalizeName(request.getName());
        validateDuplicate(userIdx, name);

        TbUser user = userRepository.findById(userIdx)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));

        TbTag tag = TbTag.builder()
                .user(user)
                .name(name)
                .color(normalizeColor(request.getColor()))
                .build();

        return TagResponse.from(tagRepository.save(tag));
    }

    @Transactional
    public TagResponse updateTag(Long userIdx, Long tagIdx, TagRequest request) {
        validateUserIdx(userIdx);
        TbTag tag = findOwnedTag(userIdx, tagIdx);
        String oldName = tag.getName();
        String newName = normalizeName(request.getName());

        if (!oldName.equals(newName)) {
            validateDuplicate(userIdx, newName);
        }

        tag.update(newName, normalizeColor(request.getColor()));
        return TagResponse.from(tag);
    }

    @Transactional
    public void deleteTag(Long userIdx, Long tagIdx) {
        validateUserIdx(userIdx);
        TbTag tag = findOwnedTag(userIdx, tagIdx);
        tagRepository.delete(tag);
    }

    private TbTag findOwnedTag(Long userIdx, Long tagIdx) {
        return tagRepository.findByIdxTagAndUserIdxUser(tagIdx, userIdx)
                .orElseThrow(() -> CustomException.notFound("태그를 찾을 수 없습니다."));
    }

    private void validateDuplicate(Long userIdx, String name) {
        if (tagRepository.existsByUserIdxUserAndName(userIdx, name)) {
            throw CustomException.badRequest("이미 존재하는 태그 이름입니다.");
        }
    }

    private void validateUserIdx(Long userIdx) {
        if (userIdx == null) {
            throw CustomException.unauthorized("로그인이 필요합니다.");
        }
    }

    private String normalizeName(String name) {
        String normalized = normalizeNullable(name);
        if (normalized == null) {
            throw CustomException.badRequest("태그 이름은 필수입니다.");
        }
        return normalized;
    }

    private String normalizeColor(String color) {
        String normalized = normalizeNullable(color);
        return normalized == null ? DEFAULT_COLOR : normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
