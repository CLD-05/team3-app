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
    private String pass;

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
}