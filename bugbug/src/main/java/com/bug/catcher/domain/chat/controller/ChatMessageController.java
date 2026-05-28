package com.bug.catcher.domain.chat.controller;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.security.ChatRoomPermissionChecker;
import com.bug.catcher.domain.chat.service.ChatMessageService;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatRoomPermissionChecker chatRoomPermissionChecker;

    /**
     * 클라이언트가 /app/chat/message 로 메시지를 발행(Publish)하면 이 메서드가 실행됩니다.
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDto.SendRequest messageRequest, Principal principal) {
        CustomUserPrincipal loginUser = getLoginUser(principal);

        if (!chatRoomPermissionChecker.isParticipant(messageRequest.getRoomId(), loginUser.getUserId())) {
            throw new AccessDeniedException("채팅방 참여자만 메시지를 보낼 수 있습니다.");
        }

        // 1. 메시지를 DB에 저장합니다.
        ChatMessageDto.Response savedMessage = chatMessageService.saveMessage(messageRequest, loginUser.getUserId());

        // 2. 해당 채팅방을 구독(Subscribe)하고 있는 모든 사용자에게 메시지를 뿌립니다(Broadcast).
        // 발행 주소: /topic/chat/room/{roomId}
        messagingTemplate.convertAndSend("/topic/chat/room/" + messageRequest.getRoomId(), savedMessage);
    }

    private CustomUserPrincipal getLoginUser(Principal principal) {
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof CustomUserPrincipal loginUser) {
            return loginUser;
        }
        throw new AccessDeniedException("로그인이 필요합니다.");
    }
}
