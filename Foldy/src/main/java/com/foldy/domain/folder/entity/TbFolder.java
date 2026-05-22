package com.foldy.domain.folder.entity;

import com.foldy.domain.user.entity.TbUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbFolder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TbFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Folder")
    private Long idxFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Idx_User", nullable = false)
    private TbUser user;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "UpdateDate", nullable = false)
    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        updateDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}