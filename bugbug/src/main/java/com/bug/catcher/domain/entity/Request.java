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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_hunter_id")
    private Hunter completedHunter;
  
    // --- 댓글 연관관계 추가 ---
    @OneToMany(mappedBy = "request", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    private String content;

    // 의뢰 수정 화면에서 입력된 기본 정보와 상태를 엔티티에 반영한다.
    public void updateDetails(
            String title,
            String content,
            String approxLocation,
            String exactLocation,
            LocalDateTime occurrenceTime,
            String description,
            String status
    ) {
        this.title = title;
        this.content = content;
        this.approxLocation = approxLocation;
        this.exactLocation = exactLocation;
        this.occurrenceTime = occurrenceTime;
        this.description = description;
        this.status = status;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    // 의뢰가 완료될 때 실제 작업을 끝낸 헌터를 함께 저장한다.
    public void completeBy(Hunter hunter) {
        this.completedHunter = hunter;
        this.status = "완료";
    }

    // 완료 상태가 아니거나 완료 헌터를 다시 지정해야 할 때 사용한다.
    public void updateCompletedHunter(Hunter hunter) {
        this.completedHunter = hunter;
    }
}
