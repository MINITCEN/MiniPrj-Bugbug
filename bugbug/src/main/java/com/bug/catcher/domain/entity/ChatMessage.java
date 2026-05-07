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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // --- 멀티미디어 지원을 위한 추가 필드 ---
    
    // 메시지 종류 (텍스트, 사진, 동영상, 음성)
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    // 사진, 영상, 음성 파일이 저장된 S3/로컬 URL 주소 (텍스트일 때는 null)
    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    // ---------------------------------------

    // 메시지 내용 (사진/영상을 보냈을 땐 "사진을 보냈습니다" 등의 안내 텍스트로 활용 가능)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 메시지 타입을 구분하기 위한 Enum
    public enum MessageType {
        TEXT,   // 일반 텍스트
        IMAGE,  // 사진
        VIDEO,  // 동영상
        AUDIO   // 음성(녹음)
    }
}
