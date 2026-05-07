package com.bug.catcher.domain.chat.service;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.repository.ChatMessageRepository;
import com.bug.catcher.domain.chat.repository.ChatRoomRepository;
import com.bug.catcher.domain.entity.ChatMessage;
import com.bug.catcher.domain.entity.ChatRoom;
import com.bug.catcher.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 프론트에서 넘어온 메시지를 DB에 저장하고 응답 형태로 반환합니다.
     */
    @Transactional
    public ChatMessageDto.Response saveMessage(ChatMessageDto.SendRequest requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        // 임시로 User를 세팅합니다. (실제로는 UserRepository에서 조회)
        User sender = User.builder().id(requestDto.getSenderId()).nickname("임시 닉네임").build();

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(requestDto.getContent())
                .messageType(requestDto.getMessageType())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        
        return ChatMessageDto.Response.fromEntity(savedMessage);
    }
}
