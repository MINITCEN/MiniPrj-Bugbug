package com.bug.catcher.domain.chat.controller;

import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 1. 채팅방 생성 (의뢰 지원 시)
     * POST /api/requests/{id}/apply
     */
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
    @GetMapping("/chats")
    public ResponseEntity<List<ChatRoomDto.ListResponse>> getMyChatRooms(
            @RequestParam Long userId, // 나중엔 세션(@SessionAttribute)에서 가져옵니다.
            @RequestParam String role) {
        
        List<ChatRoomDto.ListResponse> response = chatRoomService.getMyChatRooms(userId, role);
        return ResponseEntity.ok(response);
    }
}
