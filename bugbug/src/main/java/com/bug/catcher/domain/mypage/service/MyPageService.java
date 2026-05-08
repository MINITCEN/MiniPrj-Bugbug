package com.bug.catcher.domain.mypage.service;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.mypage.dto.MyInfoResponseDto;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {
    // 유저 정보를 DB에서 다시 확실하게 읽어오기 위해 UserRepository 주입
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyInfoResponseDto getMyInfo(Long userId) {
        // 1. DB에서 가장 최신의 유저 상태 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 2. 마이페이지용 DTO로 변환하여 반환
        return new MyInfoResponseDto(user);
    }
}
