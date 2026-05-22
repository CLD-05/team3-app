package com.foldy.domain.user.repository;
 
import com.foldy.domain.user.entity.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.util.Optional;
 
public interface UserRepository extends JpaRepository<TbUser, Long> {
 
    // 로그인/조회 — 탈퇴하지 않은(DeleteDate IS NULL) 회원만
    Optional<TbUser> findByEmailAndDeleteDateIsNull(String email);
 
    // 이메일 단순 조회
    Optional<TbUser> findByEmail(String email);
 
    // 회원가입 이메일 중복 체크
    boolean existsByEmail(String email);
}