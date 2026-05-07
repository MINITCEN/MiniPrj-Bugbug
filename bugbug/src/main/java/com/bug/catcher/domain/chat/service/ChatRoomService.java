package com.bug.catcher.domain.chat.service;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.repository.ChatMessageRepository;
import com.bug.catcher.domain.chat.repository.ChatRoomRepository;
import com.bug.catcher.domain.entity.ChatRoom;
import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    // 실제로는 RequestRepository, UserRepository, HunterRepository 도 필요합니다.

    /**
     * 헌터가 의뢰에 지원하여 채팅방을 생성합니다.
     */
    @Transactional
    public Long createChatRoom(ChatRoomDto.CreateRequest requestDto) {
        // 1. 이미 방이 있는지 확인 (중복 방지)
        if (chatRoomRepository.existsByRequestIdAndHunterId(requestDto.getRequestId(), requestDto.getHunterId())) {
            throw new IllegalArgumentException("이미 해당 의뢰에 지원하여 채팅방이 존재합니다.");
        }

        // 2. (임시) 원래는 DB에서 조회해야 함
        Request request = Request.builder().id(requestDto.getRequestId()).build();
        Hunter hunter = Hunter.builder().id(requestDto.getHunterId()).build();
        User user = User.builder().id(1L).build(); // 의뢰 올린 유저는 Request에서 가져와야 함

        // 3. 채팅방 생성 및 저장
        ChatRoom chatRoom = ChatRoom.builder()
                .request(request)
                .hunter(hunter)
                .user(user)
                .build();

        return chatRoomRepository.save(chatRoom).getId();
    }

    /**
     * 내 채팅방 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto.ListResponse> getMyChatRooms(Long userId, String role) {
        List<ChatRoom> rooms;
        
        // 역할에 따라 가져오는 방법이 다름
        if ("HUNTER".equals(role)) {
            rooms = chatRoomRepository.findByHunterId(userId);
        } else {
            rooms = chatRoomRepository.findByUserId(userId);
        }

        // 가져온 방 목록을 DTO 형태로 변환
        return rooms.stream().map(room -> ChatRoomDto.ListResponse.builder()
                .roomId(room.getId())
                .title("임시 의뢰 제목") // 실제로는 room.getRequest().getTitle()
                .otherNickname("상대방 이름") 
                .createdAt(room.getCreatedAt())
                .build()).collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 이전 메시지 내역을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto.Response> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId)
                .stream()
                .map(ChatMessageDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
}
