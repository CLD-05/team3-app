package com.foldy.domain.user.repository;

import com.foldy.domain.user.entity.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<TbUser, Long> {

    // userId로 조회 — 로그인, JWT 토큰 검증 시 사용
    Optional<TbUser> findByUserId(String userId);

    // 이메일로 조회
    Optional<TbUser> findByEmail(String email);

    // 아이디 중복 체크
    boolean existsByUserId(String userId);

    // 이메일 중복 체크
    boolean existsByEmail(String email);
}