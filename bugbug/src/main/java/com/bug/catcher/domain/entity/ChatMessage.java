package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    // 1. 어떤 채팅방에서 오간 메시지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    // 2. 메시지를 보낸 사람 (헌터도 결국 User이므로 User를 참조합니다)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // 3. 메시지 내용 (길이가 길 수 있으므로 TEXT 타입 사용)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 4. 읽음 여부 (기본값: false 안 읽음)
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // 5. 메시지 발송 시간
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // DB에 저장되기 전에 자동으로 현재 시간을 세팅합니다.
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
