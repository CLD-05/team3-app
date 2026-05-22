package com.foldy.domain.user.service;

import com.foldy.domain.user.dto.LoginRequest;
import com.foldy.domain.user.dto.LoginResponse;
import com.foldy.domain.user.dto.PasswordChangeRequest;
import com.foldy.domain.user.dto.ProfileResponse;
import com.foldy.domain.user.dto.ProfileUpdateRequest;
import com.foldy.domain.user.dto.SignupRequest;
import com.foldy.domain.user.entity.TbUser;
import com.foldy.domain.user.repository.UserRepository;
import com.foldy.global.config.JwtService;
import com.foldy.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // 회원가입 — 이메일 중복 체크 후 비번 해싱하여 저장
    @Transactional
    public void signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw CustomException.badRequest("이미 사용 중인 이메일입니다.");
        }
        String encodedPass = passwordEncoder.encode(req.password());
        TbUser user = TbUser.create(req.email(), encodedPass, req.nickname());
        userRepository.save(user);
    }

    // 로그인 — 이메일/비번 검증 후 JWT 발급
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        // 존재 여부와 비번 불일치를 같은 메시지로 처리 (계정 존재 노출 방지)
        TbUser user = userRepository.findByEmailAndDeleteDateIsNull(req.email())
                .orElseThrow(() -> CustomException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(req.pass(), user.getPassword())) {
            throw CustomException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        if (user.getStatus() != TbUser.Status.ACTIVE) {
            throw CustomException.forbidden("정상적으로 이용할 수 없는 계정입니다.");
        }

        // 토큰 subject = email, 권한은 enum 값 그대로 (이미 ROLE_ 접두사 포함)
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getIdxUser()
        );
        return new LoginResponse(token, user.getNickname(), user.getRole().name());
    }

    // 내 프로필 조회
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        return ProfileResponse.from(getActiveUser(email));
    }

    // 내 프로필 수정 (닉네임) — 더티 체킹으로 자동 반영
    @Transactional
    public void updateProfile(String email, ProfileUpdateRequest req) {
        TbUser user = getActiveUser(email);
        user.changeNickname(req.nickname());
    }

    // 비밀번호 변경 — 현재 비번 검증 후 새 비번으로 교체
    @Transactional
    public void changePassword(String email, PasswordChangeRequest req) {
        TbUser user = getActiveUser(email);
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw CustomException.badRequest("현재 비밀번호가 올바르지 않습니다.");
        }
        user.changePassword(passwordEncoder.encode(req.newPassword()));
    }
    // 회원 탈퇴 (소프트 삭제)
    @Transactional
    public void withdraw(String email) {
        TbUser user = getActiveUser(email);
        user.withdraw();
    }

    private TbUser getActiveUser(String email) {
        return userRepository.findByEmailAndDeleteDateIsNull(email)
                .orElseThrow(() -> CustomException.notFound("회원을 찾을 수 없습니다."));
    }
}