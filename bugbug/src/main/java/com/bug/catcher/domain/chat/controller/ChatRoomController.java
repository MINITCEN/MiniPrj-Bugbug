package com.bug.catcher.domain.chat.controller;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat API", description = "채팅방 및 메시지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 1. 채팅방 생성 (의뢰 지원 시)
     * POST /api/requests/{id}/apply
     */
    @Operation(summary = "채팅방 생성", description = "의뢰에 지원하며 새로운 채팅방을 생성합니다.")
    @PostMapping("/requests/{requestId}/apply")
    public ResponseEntity<Long> applyAndCreateRoom(
            @PathVariable Long requestId,
            @RequestBody ChatRoomDto.CreateRequest request) {
        
        // 프론트에서 넘어온 requestId를 DTO에 세팅해주거나 로직 조정 필요
        Long roomId = chatRoomService.createChatRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomId);
    }

    /**
     * 2. 내 채팅 목록 조회
     * GET /api/chats
     */
    @Operation(summary = "내 채팅 목록 조회", description = "현재 로그인한 유저/헌터의 채팅방 목록을 조회합니다.")
    @GetMapping("/chats")
    public ResponseEntity<List<ChatRoomDto.ListResponse>> getMyChatRooms(
            @RequestParam Long userId, // 나중엔 세션(@SessionAttribute)에서 가져옵니다.
            @RequestParam String role) {
        
        List<ChatRoomDto.ListResponse> response = chatRoomService.getMyChatRooms(userId, role);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 특정 채팅방의 이전 메시지 내역 조회
     * GET /api/chats/{roomId}/messages
     */
    @Operation(summary = "채팅방 메시지 내역 조회", description = "특정 채팅방의 과거 대화 내역을 최신순으로 불러옵니다.")
    @GetMapping("/chats/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto.Response>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page, // 향후 페이징 처리를 위해 준비
            @RequestParam(defaultValue = "20") int size) {

        List<ChatMessageDto.Response> response = chatRoomService.getMessages(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. 방문 날짜/시간 예약
     * POST /api/chats/{roomId}/reservation
     */
    @Operation(summary = "방문 날짜 예약", description = "채팅방에서 합의된 방문 날짜와 시간을 예약합니다.")
    @PostMapping("/chats/{roomId}/reservation")
    public ResponseEntity<Void> updateReservation(
            @PathVariable Long roomId,
            @RequestBody ChatRoomDto.ReservationRequest request) {
        
        chatRoomService.updateReservation(roomId, request);
        return ResponseEntity.ok().build();
    }
}
