package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    // 1. 어떤 의뢰에 대한 채팅인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    // 2. 의뢰를 올린 사람 (구매자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 3. 의뢰에 지원한 헌터 (판매자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hunter_id", nullable = false)
    private Hunter hunter;

    // 4. 채팅방 생성 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 5. 방문 예약 날짜/시간
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    // DB에 저장되기 전에 자동으로 현재 시간을 세팅합니다.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 예약 날짜 업데이트 메서드
    public void updateReservation(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }
}
