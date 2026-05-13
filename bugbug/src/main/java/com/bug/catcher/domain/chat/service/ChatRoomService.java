package com.bug.catcher.domain.chat.service;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.repository.ChatMessageRepository;
import com.bug.catcher.domain.chat.repository.ChatRoomRepository;
import com.bug.catcher.domain.entity.ChatRoom;
import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.repository.RequestRepository;
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
    private final RequestRepository requestRepository;

    /**
     * 헌터가 의뢰에 지원하여 채팅방을 생성합니다.
     */
    @Transactional
    public Long createChatRoom(ChatRoomDto.CreateRequest requestDto) {
        // 1. 이미 방이 있는지 확인 (중복 방지)
        if (chatRoomRepository.existsByRequestIdAndHunterId(requestDto.getRequestId(), requestDto.getHunterId())) {
            throw new IllegalArgumentException("이미 해당 의뢰에 지원하여 채팅방이 존재합니다.");
        }

        // 2. DB에서 실제 의뢰글을 찾아와서 의뢰인(작업 요청자) 정보를 꺼냅니다.
        Request request = requestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 의뢰입니다."));
                
        Hunter hunter = Hunter.builder().id(requestDto.getHunterId()).build();
        User user = request.getUser(); // 임시 1번 유저 땜빵 삭제! 의뢰글 주인이 진짜 방 주인이 됨

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
            // 프론트에서 넘어온 userId를 기준으로, 그 유저와 연결된 헌터의 채팅방을 찾습니다.
            rooms = chatRoomRepository.findByHunter_UserId(userId);
        } else {
            rooms = chatRoomRepository.findByUserId(userId);
        }

        // 가져온 방 목록을 DTO 형태로 변환
        return rooms.stream().map(room -> {
            // 진짜 의뢰 제목과 상대방 닉네임을 꺼내옵니다 (땜빵 코드 삭제)
            String title = room.getRequest().getTitle();
            String otherNickname = "HUNTER".equals(role) ? room.getUser().getNickname() : room.getHunter().getName();
            
            return ChatRoomDto.ListResponse.builder()
                    .roomId(room.getId())
                    .title(title)
                    .otherNickname(otherNickname)
                    .createdAt(room.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 이전 메시지 내역을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto.Response> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId)
                .stream()
                .map(ChatMessageDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 채팅방에서 합의된 방문 날짜/시간을 예약합니다.
     */
    @Transactional
    public void updateReservation(Long roomId, ChatRoomDto.ReservationRequest requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        
        chatRoom.updateReservation(requestDto.getReservedAt());
        // 별도의 save 호출 없이 더티 체킹(Dirty Checking)으로 업데이트됩니다.
    }
}
