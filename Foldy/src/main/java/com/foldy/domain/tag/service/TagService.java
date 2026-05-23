package com.foldy.domain.tag.service;

import com.foldy.domain.memo.entity.TbMemo;
import com.foldy.domain.memo.repository.MemoRepository;
import com.foldy.domain.tag.dto.TagRequest;
import com.foldy.domain.tag.dto.TagResponse;
import com.foldy.domain.tag.entity.TbTag;
import com.foldy.domain.tag.repository.TagRepository;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.domain.user.repository.UserRepository;
import com.foldy.global.exception.CustomException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;
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
	// 태그-메모 연결 기능 추가: 태그 삭제 시 tbMemo.Tags 문자열 정리용
	private final MemoRepository memoRepository;

	public List<TagResponse> getTags(Long userIdx) {
		// validateUserIdx(userIdx); 제거
		Map<Long, Long> memoCountByTagId = countMemosByTagId(userIdx);
		return tagRepository.findByUserIdxUserOrderByCreateDateDesc(userIdx).stream()
				.map(tag -> TagResponse.from(tag, memoCountByTagId.getOrDefault(tag.getIdxTag(), 0L))).toList();
	}

	@Transactional
	public TagResponse createTag(Long userIdx, TagRequest request) {
		String name = normalizeName(request.getName());
		validateDuplicate(userIdx, name);

		TbUser user = userRepository.findById(userIdx).orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다."));

		TbTag tag = TbTag.builder().user(user).name(name).color(normalizeColor(request.getColor())).build();

		return TagResponse.from(tagRepository.save(tag));
	}

	@Transactional
	public TagResponse updateTag(Long userIdx, Long tagIdx, TagRequest request) {
		TbTag tag = findOwnedTag(userIdx, tagIdx);
		String oldName = tag.getName();
		String newName = normalizeName(request.getName());

		if (!oldName.equals(newName)) {
			validateDuplicate(userIdx, newName);
		}

		tag.update(newName, normalizeColor(request.getColor()));
		// 태그-메모 연결 기능 추가: tbMemo.Tags는 태그 ID 저장 방식이라 이름 변경 전파는 필요 없음
		return TagResponse.from(tag);
	}

	@Transactional
	public void deleteTag(Long userIdx, Long tagIdx) {
		TbTag tag = findOwnedTag(userIdx, tagIdx);
		removeTagFromUserMemos(userIdx, tagIdx);
		tagRepository.delete(tag);
	}

	// 태그-메모 연결 기능 추가: 삭제된 태그 ID를 사용자의 모든 메모 Tags 문자열에서 제거
	private void removeTagFromUserMemos(Long userIdx, Long tagIdx) {
		String tagIdValue = String.valueOf(tagIdx);
		for (TbMemo memo : memoRepository.findByUser_IdxUser(userIdx)) {
			List<String> remainingTagIds = parseTagIdValues(memo.getTags()).stream()
					.filter(tagId -> !tagId.equals(tagIdValue)).toList();
			memo.updateTags(joinTagIdValues(remainingTagIds));
		}
	}

	// 태그-메모 연결 기능 추가: tbMemo.Tags에 저장된 태그 ID 기준으로 태그별 메모 개수 계산
	private Map<Long, Long> countMemosByTagId(Long userIdx) {
		Map<Long, Long> counts = new HashMap<>();
		for (TbMemo memo : memoRepository.findByUser_IdxUser(userIdx)) {
			for (String tagIdValue : parseTagIdValues(memo.getTags())) {
				try {
					Long tagId = Long.parseLong(tagIdValue);
					counts.merge(tagId, 1L, Long::sum);
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return counts;
	}

	// 태그-메모 연결 기능 추가: tbMemo.Tags의 ID 문자열을 중복 없는 목록으로 변환
	private List<String> parseTagIdValues(String tags) {
		if (tags == null || tags.isBlank()) {
			return Collections.emptyList();
		}
		Set<String> tagIds = new LinkedHashSet<>();
		for (String tagId : tags.split(",")) {
			String normalized = tagId.trim();
			if (!normalized.isEmpty())
				tagIds.add(normalized);
		}
		return new ArrayList<>(tagIds);
	}

	// 태그-메모 연결 기능 추가: ID 목록을 tbMemo.Tags 저장 형식으로 변환
	private String joinTagIdValues(List<String> tagIds) {
		if (tagIds == null || tagIds.isEmpty()) {
			return null;
		}
		return tagIds.stream().collect(Collectors.joining(","));
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

	// ### validateUserIdx 호출 제거
	// ### TagApiController에서 이미 requireUserIdx()로 null 체크를 합니다.
	// ### Service에서 중복 체크할 필요 없습니다.

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
