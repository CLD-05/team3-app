package com.foldy.domain.memo.entity;

import com.foldy.domain.folder.entity.TbFolder;
import com.foldy.domain.user.entity.TbUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbMemo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TbMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Memo")
    private Long idxMemo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Idx_Folder", nullable = false)
    private TbFolder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Idx_User", nullable = false)
    private TbUser user;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Lob
    @Column(name = "Content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "Tags", length = 500)
    private String tags;

    @OneToMany(mappedBy = "memo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TbMemoImage> images = new ArrayList<>();

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

    public void update(String title, String content, String tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }
}