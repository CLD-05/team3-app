package com.foldy.domain.stats.entity;

import com.foldy.domain.user.entity.TbUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbAiLog")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TbAiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_AiLog")
    private Long idxAiLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Idx_User", nullable = false)
    private TbUser user;

    @Lob
    @Column(name = "SummaryContent", nullable = false, columnDefinition = "LONGTEXT")
    private String summaryContent;

    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
}