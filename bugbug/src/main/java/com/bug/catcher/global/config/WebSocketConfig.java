package com.bug.catcher.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
}
