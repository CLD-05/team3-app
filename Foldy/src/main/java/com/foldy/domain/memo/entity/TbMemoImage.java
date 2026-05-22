package com.foldy.domain.memo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbMemoImage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TbMemoImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_MemoImage")
    private Long idxMemoImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Idx_Memo", nullable = false)
    private TbMemo memo;

    @Column(name = "S3Url", nullable = false, length = 500)
    private String s3Url;

    @Column(name = "FileName", nullable = false, length = 255)
    private String fileName;

    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
}