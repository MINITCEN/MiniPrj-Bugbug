package com.bug.catcher.domain.user.service;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.dto.SignupRequestDto;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 1. 이메일 중복 체크
        // (만약 UserRepository에 findByEmail을 안 만드셨다면 에러가 납니다! 이전 답변을 참고해 꼭 추가해주세요)
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        // 2. 비밀번호 암호화 (현재는 임시로 원본 저장, 나중에 Spring Security 추가 시 변경 필요!)
        String password = requestDto.getPassword();

        // 3. User 엔티티 생성 (권한은 무조건 "USER"로 강제 세팅)
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(password)
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .role("USER") // 엔티티의 role 타입이 String이므로 이렇게 고정합니다.
                .build();

        // 4. DB에 저장
        userRepository.save(user);
    }
}