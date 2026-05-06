package com.bug.catcher.domain.chat.repository;

import com.bug.catcher.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 1. 의뢰인(일반 유저)이 자신이 참여한 채팅방 목록을 찾을 때 사용합니다.
    List<ChatRoom> findByUserId(Long userId);

    // 2. 헌터가 자신이 참여한 채팅방 목록을 찾을 때 사용합니다.
    List<ChatRoom> findByHunterId(Long hunterId);

    // 3. 특정 의뢰에 대해 특정 헌터가 이미 만들어둔 방이 있는지 확인합니다. (중복 방 생성 방지용)
    boolean existsByRequestIdAndHunterId(Long requestId, Long hunterId);
}
