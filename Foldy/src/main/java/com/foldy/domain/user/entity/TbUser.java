package com.foldy.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbUser")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TbUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_User")
    private Long idxUser;

    @Column(name = "Email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "Pass", nullable = false, length = 255)
    private String password;

    @Column(name = "Nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = false)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UpdateDate", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "DeleteDate")
    private LocalDateTime deleteDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        updateDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }

    public enum Role { ROLE_USER, ROLE_ADMIN }
    public enum Status { ACTIVE, SUSPENDED }
    
 // 회원가입용 정적 팩토리 — pass 에는 반드시 인코딩(BCrypt)된 값을 넘길 것
    public static TbUser create(String email, String encodedPass, String nickname) {
        return TbUser.builder()
                .email(email)
                .password(encodedPass)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .status(Status.ACTIVE)
                .build();
    }
 
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
 
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
 
    // 탈퇴 = DeleteDate 기록 (행은 보존하는 소프트 삭제)
    public void withdraw() {
        this.deleteDate = LocalDateTime.now();
        this.status = Status.SUSPENDED;
    }
 
    public boolean isWithdrawn() {
        return this.deleteDate != null;
    }
 
    public boolean isActive() {
        return this.deleteDate == null && this.status == Status.ACTIVE;
    }
}