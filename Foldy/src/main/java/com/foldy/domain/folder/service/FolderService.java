package com.foldy.domain.folder.service;

import com.foldy.domain.folder.dto.FolderResponse;
import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.folder.repository.FolderRepository;
import com.foldy.domain.memo.repository.MemoRepository;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.domain.user.repository.UserRepository;
import com.foldy.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final MemoRepository memoRepository;

    @Transactional
    public Long createFolder(Long userId, String folderName) {
        TbUser user = userRepository.findById(userId)
                // ### new IllegalArgumentException → CustomException.notFound 로 변경
                // ### CustomException을 사용하면 GlobalExceptionHandler가 자동으로 잡아서
                // ### 적절한 HTTP 상태코드(404)와 함께 fail 응답을 반환합니다.
                // ### IllegalArgumentException은 400으로만 처리되어 상황에 맞지 않습니다.
                .orElseThrow(() -> CustomException.notFound("존재하지 않는 유저입니다."));

        TbFolder folder = TbFolder.builder()
                .user(user)
                .name(folderName)
                .build();

        return folderRepository.save(folder).getIdxFolder();
    }

    // ### 반환 타입 List<TbFolder> → List<FolderResponse> 로 변경
    // ### 엔티티를 직접 반환하면 LAZY 로딩 연관관계 직렬화 에러가 발생할 수 있습니다.
    // ### stream().map(FolderResponse::from) 으로 DTO 변환 후 반환합니다.
    // ### FolderResponse::from 은 FolderResponse.from(folder) 의 메서드 참조 표현입니다.
    public List<FolderResponse> getFolderList(Long userId) {
        return folderRepository.findByUserIdxUserOrderByCreateDateDesc(userId)
                .stream()
                .map(FolderResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateFolder(Long folderId, String newName) {
        TbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> CustomException.notFound("존재하지 않는 폴더입니다."));
        // ### 더티 체킹(Dirty Checking)
        // ### @Transactional 범위 안에서 엔티티 필드를 변경하면
        // ### 트랜잭션 종료 시점에 JPA가 자동으로 UPDATE 쿼리를 실행합니다.
        // ### folderRepository.save() 를 별도로 호출할 필요가 없습니다.
        folder.updateName(newName);
    }

    
    @Transactional
    public boolean deleteFolder(Long folderId) {
        // 1. 존재하는 폴더인지 먼저 검증 (기존 예외 처리 유지)
        TbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> CustomException.notFound("존재하지 않는 폴더입니다."));

        // 2. 🎯 엔티티 변수명(folder)에 맞춰 레포지토리 메서드 호출하도록 수정 완료!
        long memoCount = memoRepository.countByFolder(folder); 

        // 3. 메모가 1개라도 남아있다면 삭제를 진행하지 않고 false 리턴!
        if (memoCount > 0) {
            return false; 
        }

        // 4. 메모가 0개일 때만 안전하게 폴더를 삭제
        folderRepository.delete(folder);
        return true;
    }
}