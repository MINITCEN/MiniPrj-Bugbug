package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HunterApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 신청서에 입력한 정보들 (기존 유저 정보에서 가져오되 수정 가능)
    private String name;

    @Column(nullable = false)
    private Boolean pledgeAgreed;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt;

    // 관리자가 승인/거절 시 상태를 변경하는 메서드
    public void updateStatus(ApplicationStatus status) {
        this.status = status;
    }


}
