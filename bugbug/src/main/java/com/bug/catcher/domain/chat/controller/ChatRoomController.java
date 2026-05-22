package com.bug.catcher.domain.chat.controller;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.service.ChatMessageService;
import com.bug.catcher.domain.chat.service.ChatRoomService;
import com.bug.catcher.domain.entity.ChatMessage;
import com.bug.catcher.global.infra.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "Chat API", description = "채팅방 및 메시지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final FileService fileService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 1. 채팅방 생성 (의뢰 지원 시)
     * POST /api/requests/{id}/apply
     */
    @Operation(summary = "채팅방 생성", description = "의뢰에 지원하며 새로운 채팅방을 생성합니다.")
    @PostMapping("/requests/{requestId}/apply")
    public ResponseEntity<Long> applyAndCreateRoom(
            @PathVariable("requestId") Long requestId,
            @SessionAttribute(com.bug.catcher.global.auth.SessionConst.LOGIN_USER) com.bug.catcher.domain.entity.User loginUser) {
        
        // 세션의 유저 ID를 이용해 백엔드에서 안전하게 헌터 정보를 찾고 방을 생성합니다.
        Long roomId = chatRoomService.createChatRoom(requestId, loginUser.getId());
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

    /**
     * 5. 파일 전송 (사진, 동영상, 음성)
     * POST /api/chats/{roomId}/files
     */
    @Operation(summary = "채팅 파일 전송", description = "사진, 동영상, 녹음 파일을 전송합니다.")
    @PostMapping(value = "/chats/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadChatFile(
            @PathVariable Long roomId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("messageType") ChatMessage.MessageType messageType,
            @RequestParam("senderId") Long senderId) {

        try {
            // 1. 로컬(uploads 폴더)에 파일 저장 후 URL 경로 반환 (확장자 검증 포함)
            String fileUrl = fileService.storeFile(file, messageType);

            // 2. DB에 메시지(파일 URL 포함) 저장
            ChatMessageDto.Response savedMessage = chatMessageService.saveFileMessage(roomId, senderId, fileUrl, messageType);

            // 3. 웹소켓을 통해 채팅방에 메시지 브로드캐스트
            messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, savedMessage);

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            // 확장자가 안 맞아서 에러가 난 경우, 프론트에 400 Bad Request와 에러 메시지를 보냅니다.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 중 오류가 발생했습니다.");
        }
    }
}
