package com.bug.catcher.domain.chat.repository;

import com.bug.catcher.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. 특정 채팅방의 모든 메시지를 최신순(내림차순)으로 가져옵니다.
    // 리스트의 0번째가 가장 최근 메시지가 됩니다.
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(Long roomId);
}
