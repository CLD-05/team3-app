package com.foldy.domain.folder.repository;

import com.foldy.domain.folder.entity.TbFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<TbFolder, Long> {

    // ### OrderByCreateDateDesc 추가
    // ### 폴더 목록을 최신 생성순으로 정렬해서 반환합니다.
    // ### Service에서 별도로 정렬 로직을 작성할 필요 없이
    // ### 메서드 이름만으로 정렬 조건을 지정할 수 있습니다.
    List<TbFolder> findByUserIdxUserOrderByCreateDateDesc(Long idxUser);
}