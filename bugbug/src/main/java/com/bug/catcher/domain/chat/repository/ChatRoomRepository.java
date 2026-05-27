package com.bug.catcher.domain.chat.repository;

import com.bug.catcher.domain.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c WHERE c.user.id = :userId")
    List<ChatRoom> findByUser_Id(@Param("userId") Long userId);

    @Query("SELECT c FROM ChatRoom c WHERE c.hunter.user.id = :userId")
    List<ChatRoom> findByHunter_User_Id(@Param("userId") Long userId);

    boolean existsByRequestIdAndHunterId(Long requestId, Long hunterId);

    @Query("SELECT COUNT(c) > 0 FROM ChatRoom c WHERE c.id = :roomId AND (c.user.id = :userId OR c.hunter.user.id = :userId)")
    boolean existsParticipant(@Param("roomId") Long roomId, @Param("userId") Long userId);

    // 의뢰 완료 처리 시 예약 완료된 채팅방의 헌터를 완료 헌터로 자동 지정한다.
    List<ChatRoom> findByRequestIdAndReservedAtIsNotNullOrderByReservedAtDesc(Long requestId);

    // 한 의뢰에 예약 완료 채팅방이 여러 개 생기지 않게 막는다.
    boolean existsByRequestIdAndReservedAtIsNotNullAndIdNot(Long requestId, Long roomId);
}
