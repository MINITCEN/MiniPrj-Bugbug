package com.bug.catcher.domain.user.service;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.dto.SignupRequestDto;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDto requestDto) {
        // 1. 개인정보 필수 동의 체크 검증
        if (requestDto.getIsPrivacyAgreed() == null || !requestDto.getIsPrivacyAgreed()) {
            throw new IllegalArgumentException("개인정보 수집 및 이용에 동의해야 회원가입이 가능합니다.");
        }

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 3. 닉네임 중복 체크 추가
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다. 다른 닉네임을 사용해 주세요.");
        }
        // 4. 비밀번호 암호화 수행
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 3. User 엔티티 생성 및 저장
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .role("USER")
                .isPrivacyAgreed(requestDto.getIsPrivacyAgreed()) // 동의 내역 저장
                .build();

        userRepository.save(user);
    }
}