package com.bug.catcher.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class ChatRoomDto {

    // 1. 채팅방 생성 요청 DTO (헌터가 '지원하기' 누를 때)
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long requestId; // 지원할 의뢰의 ID
        private Long hunterId;  // 지원하는 헌터의 ID (나중엔 세션에서 가져올 수도 있음)
    }

    // 2. 채팅방 목록 조회 응답 DTO (카카오톡 채팅방 목록처럼 보여줄 데이터)
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long roomId;
        private String title;          // 의뢰 제목
        private String otherNickname;  // 상대방 닉네임 (유저에겐 헌터이름, 헌터에겐 유저이름)
        private LocalDateTime createdAt;
        private LocalDateTime reservedAt; // 방문 예약 날짜/시간
    }

    // 3. 방문 날짜/시간 예약 요청 DTO
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationRequest {
        private LocalDateTime reservedAt;
    }
}
