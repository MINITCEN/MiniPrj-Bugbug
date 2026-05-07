package com.bug.catcher.domain.chat.dto;

import com.bug.catcher.domain.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ChatMessageDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long messageId;
        private Long senderId;
        private String senderNickname;
        private ChatMessage.MessageType messageType;
        private String fileUrl;
        private String content;
        private boolean isRead;
        private LocalDateTime createdAt;

        public static Response fromEntity(ChatMessage message) {
            return Response.builder()
                    .messageId(message.getId())
                    .senderId(message.getSender().getId())
                    .senderNickname(message.getSender().getNickname())
                    .messageType(message.getMessageType())
                    .fileUrl(message.getFileUrl())
                    .content(message.getContent())
                    .isRead(message.isRead())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }
}
