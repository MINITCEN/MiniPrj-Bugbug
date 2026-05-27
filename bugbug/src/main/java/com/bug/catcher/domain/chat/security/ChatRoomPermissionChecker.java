package com.bug.catcher.domain.chat.security;

import com.bug.catcher.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRoomPermissionChecker {

    private final ChatRoomRepository chatRoomRepository;

    public boolean isParticipant(Long roomId, Long userId) {
        return roomId != null
                && userId != null
                && chatRoomRepository.existsParticipant(roomId, userId);
    }
}
