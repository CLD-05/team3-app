package com.foldy.domain.tag.entity;

import com.foldy.domain.user.entity.TbUser;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbTag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TbTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Tag")
    private Long idxTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Idx_User", nullable = false)
    private TbUser user;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Color", nullable = false, length = 10)
    @Builder.Default
    private String color = "#4ECDC4";

    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }
}