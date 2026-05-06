package com.bug.catcher.domain.chat.repository;

import com.bug.catcher.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. 특정 채팅방의 모든 메시지를 과거순(오름차순)으로 가져옵니다.
    // 채팅방에 입장했을 때 위에서부터 순서대로 메시지를 보여주기 위해 사용합니다.
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long roomId);
}
