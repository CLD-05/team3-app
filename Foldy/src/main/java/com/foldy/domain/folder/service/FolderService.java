package com.foldy.domain.folder.service;

import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.folder.repository.FolderRepository;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository; // 유저 확인용

    // 1. 폴더 생성
    @Transactional
    public Long createFolder(Long userId, String folderName) {
        TbUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        TbFolder folder = TbFolder.builder()
                .user(user)
                .name(folderName)
                .build();

        folderRepository.save(folder);

        return folderRepository.save(folder).getIdxFolder();
    }

    // 2. 폴더 목록 조회
    public List<TbFolder> getFolderList(Long userId) {
        return folderRepository.findByUserIdxUser(userId);
    }

    // 3. 폴더 수정
    @Transactional
    public void updateFolder(Long folderId, String newName) {
        TbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        folder.updateName(newName); // 더티 체킹(Dirty Checking)으로 자동 업데이트
    }

    // 4. 폴더 삭제
    @Transactional
    public void deleteFolder(Long folderId) {
        TbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        folderRepository.delete(folder);
    }
}