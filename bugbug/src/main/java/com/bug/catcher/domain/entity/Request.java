package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequestImage> requestImages = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String description;

    private String approxLocation;
    private String exactLocation;
    private String title;

    private String videoUrl;
    private Integer viewCount;
    private String status;
    private LocalDateTime occurrenceTime;

    // 본문 내용에 태그가 포함되어 들어가도록 구성
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    // --- 댓글 연관관계 추가 ---
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    public void updateStatus(String status) {
        this.status = status;
    }
}