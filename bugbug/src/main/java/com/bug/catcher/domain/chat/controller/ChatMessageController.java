package com.bug.catcher.domain.chat.controller;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    /**
     * 클라이언트가 /app/chat/message 로 메시지를 발행(Publish)하면 이 메서드가 실행됩니다.
     */
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageDto.SendRequest messageRequest) {

        // 1. 메시지를 DB에 저장합니다.
        ChatMessageDto.Response savedMessage = chatMessageService.saveMessage(messageRequest);

        // 2. 해당 채팅방을 구독(Subscribe)하고 있는 모든 사용자에게 메시지를 뿌립니다(Broadcast).
        // 발행 주소: /topic/chat/room/{roomId}
        messagingTemplate.convertAndSend("/topic/chat/room/" + messageRequest.getRoomId(), savedMessage);
    }
}
