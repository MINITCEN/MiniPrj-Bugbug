package com.bug.catcher.domain.chat.controller;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.service.ChatMessageService;
import com.bug.catcher.domain.chat.service.ChatRoomService;
import com.bug.catcher.domain.entity.ChatMessage;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import com.bug.catcher.global.infra.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Chat API", description = "채팅방 및 메시지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final FileService fileService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "채팅방 생성", description = "요청에 지원하면 새 채팅방을 생성합니다.")
    @PreAuthorize("hasRole('HUNTER')")
    @PostMapping("/requests/{requestId}/apply")
    public ResponseEntity<Long> applyAndCreateRoom(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable("requestId") Long requestId) {

        Long roomId = chatRoomService.createChatRoom(requestId, loginUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(roomId);
    }

    @Operation(summary = "내 채팅 목록 조회", description = "현재 로그인한 사용자의 채팅방 목록을 조회합니다.")
    @GetMapping("/chats")
    public ResponseEntity<List<ChatRoomDto.ListResponse>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserPrincipal loginUser) {

        List<ChatRoomDto.ListResponse> response = chatRoomService.getMyChatRooms(
                loginUser.getUserId(),
                loginUser.getRole()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅방 메시지 내역 조회", description = "특정 채팅방의 과거 메시지를 조회합니다.")
    @PreAuthorize("@chatRoomPermissionChecker.isParticipant(#roomId, principal.userId)")
    @GetMapping("/chats/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto.Response>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ChatMessageDto.Response> response = chatRoomService.getMessages(roomId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "방문 날짜 예약", description = "채팅방에서 합의한 방문 날짜와 시간을 예약합니다.")
    @PreAuthorize("@chatRoomPermissionChecker.isParticipant(#roomId, principal.userId)")
    @PostMapping("/chats/{roomId}/reservation")
    public ResponseEntity<Void> updateReservation(
            @PathVariable Long roomId,
            @RequestBody ChatRoomDto.ReservationRequest request) {

        chatRoomService.updateReservation(roomId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "채팅 파일 전송", description = "사진, 동영상, 음성 파일을 전송합니다.")
    @PreAuthorize("@chatRoomPermissionChecker.isParticipant(#roomId, principal.userId)")
    @PostMapping(value = "/chats/{roomId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadChatFile(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long roomId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("messageType") ChatMessage.MessageType messageType) {

        try {
            String fileUrl = fileService.storeFile(file, messageType);
            ChatMessageDto.Response savedMessage = chatMessageService.saveFileMessage(roomId, loginUser.getUserId(), fileUrl, messageType);
            messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, savedMessage);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 중 오류가 발생했습니다.");
        }
    }
}
