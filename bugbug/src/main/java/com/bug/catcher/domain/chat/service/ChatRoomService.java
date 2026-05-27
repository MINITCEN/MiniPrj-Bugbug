package com.bug.catcher.domain.chat.service;

import com.bug.catcher.domain.chat.dto.ChatMessageDto;
import com.bug.catcher.domain.chat.dto.ChatRoomDto;
import com.bug.catcher.domain.chat.repository.ChatMessageRepository;
import com.bug.catcher.domain.chat.repository.ChatRoomRepository;
import com.bug.catcher.domain.entity.ChatRoom;
import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.domain.hunter.repository.ApplicationRepository;
import com.bug.catcher.domain.entity.Application;
import jakarta.persistence.EntityNotFoundException;
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
    private final HunterRepository hunterRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * 헌터가 의뢰에 지원하여 채팅방을 생성합니다.
     */
    @Transactional
    public Long createChatRoom(Long requestId, Long hunterUserId) {
        // 헌터의 유저 ID로 헌터 정보를 찾습니다.
        Hunter hunter = hunterRepository.findTopByUserIdOrderByIdDesc(hunterUserId)
                .orElseThrow(() -> new IllegalArgumentException("헌터 정보가 존재하지 않습니다."));

        // 1. 이미 방이 있는지 확인 (중복 방지)
        if (chatRoomRepository.existsByRequestIdAndHunterId(requestId, hunter.getId())) {
            throw new IllegalArgumentException("이미 해당 의뢰에 지원하여 채팅방이 존재합니다.");
        }

        // 2. DB에서 실제 의뢰글을 찾아와서 의뢰인(작업 요청자) 정보를 꺼냅니다.
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 의뢰입니다."));
                
        User user = request.getUser(); // 의뢰글 주인이 진짜 방 주인이 됨

        // 본인의 의뢰에 본인이 헌터로 지원하는 것 방지
        if (user.getId().equals(hunterUserId)) {
            throw new IllegalArgumentException("본인이 작성한 의뢰에는 지원할 수 없습니다.");
        }

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
            rooms = chatRoomRepository.findByHunter_User_Id(userId);
        } else {
            rooms = chatRoomRepository.findByUser_Id(userId);
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
                    .reservedAt(room.getReservedAt())
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
        // 1. 채팅방 검증
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 채팅방입니다. ROOM_ID: " + roomId));
        
        // 2. 예약 일자 업데이트 (더티 체킹 보장)
        chatRoom.updateReservation(requestDto.getReservedAt());
        
        // 3. [Early Return 적용] 연관 의뢰(Request) 검증 및 예외 처리
        Request request = chatRoom.getRequest();
        if (request == null) {
            throw new EntityNotFoundException("채팅방에 매핑된 의뢰 정보가 존재하지 않습니다. ROOM_ID: " + roomId);
        }

        // 한 의뢰는 한 명의 헌터와만 예약 완료될 수 있다.
        if (chatRoomRepository.existsByRequestIdAndReservedAtIsNotNullAndIdNot(request.getId(), roomId)) {
            throw new IllegalStateException("이미 다른 헌터와 예약 완료된 의뢰입니다.");
        }
        
        // 4. 의뢰 상태 변경
        request.updateStatus("예약 완료");
        
        // 5. [Early Return 적용] 연관 헌터(Hunter) 검증 및 예외 처리
        Hunter hunter = chatRoom.getHunter();
        if (hunter == null) {
            throw new EntityNotFoundException("채팅방에 매핑된 헌터 정보가 존재하지 않습니다. ROOM_ID: " + roomId);
        }
        
        // 6. 중복 확인 후 매칭 내역(Application) 추가
        boolean isAlreadyApplied = applicationRepository.existsByRequestIdAndHunterId(request.getId(), hunter.getId());
        if (!isAlreadyApplied) {
            Application application = Application.builder()
                    .request(request)
                    .hunter(hunter)
                    .build();
            applicationRepository.save(application);
        }
        // 별도의 save 호출 없이 더티 체킹(Dirty Checking)으로 업데이트됩니다.
    }
}
