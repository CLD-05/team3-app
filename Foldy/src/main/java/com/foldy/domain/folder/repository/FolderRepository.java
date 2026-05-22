package com.foldy.domain.folder.repository;

import com.foldy.domain.folder.entity.TbFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<TbFolder, Long> {
    
    // findBy + User(연관필드명) + IdxUser(TbUser의실제변수명)
    List<TbFolder> findByUserIdxUser(Long idxUser);
}