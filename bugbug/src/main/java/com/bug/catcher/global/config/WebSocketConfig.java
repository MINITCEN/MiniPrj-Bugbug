package com.bug.catcher.global.config;

import com.bug.catcher.domain.chat.security.ChatRoomPermissionChecker;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String CHAT_ROOM_TOPIC_PREFIX = "/topic/chat/room/";

    private final ChatRoomPermissionChecker chatRoomPermissionChecker;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 프론트엔드에서 웹소켓 연결 시 사용할 엔드포인트 주소: ws://localhost:8080/ws/chats
        registry.addEndpoint("/ws/chats")
                .setAllowedOriginPatterns("*") // 실무에서는 특정 도메인만 허용하도록 변경 필요
                .withSockJS(); // 구형 브라우저 지원을 위해 SockJS 옵션 추가
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트(프론트)가 메시지를 보낼 때 (발신) 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");

        // 클라이언트(프론트)가 메시지를 받을 때 (수신/구독) 사용할 prefix
        registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
                    validateSubscription(accessor);
                }

                return message;
            }
        });
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(CHAT_ROOM_TOPIC_PREFIX)) {
            return;
        }

        CustomUserPrincipal loginUser = getLoginUser(accessor);
        Long roomId = parseRoomId(destination);

        if (!chatRoomPermissionChecker.isParticipant(roomId, loginUser.getUserId())) {
            throw new AccessDeniedException("채팅방 참여자만 구독할 수 있습니다.");
        }
    }

    private CustomUserPrincipal getLoginUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof Authentication authentication
                && authentication.getPrincipal() instanceof CustomUserPrincipal loginUser) {
            return loginUser;
        }
        throw new AccessDeniedException("로그인이 필요합니다.");
    }

    private Long parseRoomId(String destination) {
        String roomId = destination.substring(CHAT_ROOM_TOPIC_PREFIX.length());
        try {
            return Long.valueOf(roomId);
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("올바르지 않은 채팅방 구독 요청입니다.");
        }
    }
}
