package com.foldy.domain.tag.repository;

import com.foldy.domain.tag.entity.TbTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TbTag, Long> {

    List<TbTag> findByUserIdxUserOrderByCreateDateDesc(Long userIdx);

    Optional<TbTag> findByIdxTagAndUserIdxUser(Long tagIdx, Long userIdx);

    boolean existsByUserIdxUserAndName(Long userIdx, String name);
}
