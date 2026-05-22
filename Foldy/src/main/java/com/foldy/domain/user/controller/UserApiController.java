package com.foldy.domain.user.controller;

import com.foldy.domain.user.dto.LoginRequest;
import com.foldy.domain.user.dto.LoginResponse;
import com.foldy.domain.user.dto.PasswordChangeRequest;
import com.foldy.domain.user.dto.ProfileResponse;
import com.foldy.domain.user.dto.ProfileUpdateRequest;
import com.foldy.domain.user.dto.SignupRequest;
import com.foldy.domain.user.service.UserService;
import com.foldy.global.controller.BaseController;
import com.foldy.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserApiController extends BaseController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest req) {
        userService.signup(req);
        return ok();
    }

    // 로그인 — 토큰을 응답 바디 + HttpOnly 쿠키 둘 다로 내려줌
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse res = userService.login(req);
        ResponseCookie cookie = buildTokenCookie(res.token(), Duration.ofDays(1));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(res));
    }

    // 로그아웃 — 토큰 쿠키 만료 (JWT는 stateless라 클라이언트 토큰 폐기 방식)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        ResponseCookie expired = buildTokenCookie("", Duration.ZERO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(ApiResponse.success());
    }

    // 내 프로필 조회 (인증 필요)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {
        return ok(userService.getProfile(getCurrentUserId()));
    }

    // 내 프로필 수정 (인증 필요)
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(@Valid @RequestBody ProfileUpdateRequest req) {
        userService.updateProfile(getCurrentUserId(), req);
        return ok();
    }

    // 비밀번호 변경 (인증 필요)
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        userService.changePassword(getCurrentUserId(), req);
        return ok();
    }
    // 회원 탈퇴 (인증 필요) — 탈퇴 후 토큰 쿠키도 만료
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw() {
        userService.withdraw(getCurrentUserId());
        ResponseCookie expired = buildTokenCookie("", Duration.ZERO);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(ApiResponse.success());
    }

//     토큰 쿠키 생성 헬퍼
    private ResponseCookie buildTokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from("token", value)
                .httpOnly(true)   // JS에서 접근 불가 (XSS로 토큰 탈취 방어)
                .secure(true)     // HTTPS 전송만. 로컬 http 테스트 시에만 false
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}