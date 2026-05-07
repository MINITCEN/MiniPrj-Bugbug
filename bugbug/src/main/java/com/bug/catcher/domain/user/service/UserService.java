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
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 닉네임 중복 체크 추가
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다. 다른 닉네임을 사용해 주세요.");
        }

        // 3. User 엔티티 생성 및 저장 (비밀번호 암호화는 나중에 추가)
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .role("USER")
                .build();

        userRepository.save(user);
    }
}