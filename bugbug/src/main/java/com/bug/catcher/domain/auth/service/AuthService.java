package com.bug.catcher.domain.auth.service;

import com.bug.catcher.domain.auth.dto.LoginRequestDto;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Auth 도메인이지만, 유저 정보를 확인해야 하므로 User 도메인의 리포지토리를 가져옵니다!
    private final UserRepository userRepository;

    // 읽기 전용 트랜잭션 (데이터를 수정하지 않고 조회만 할 때 성능이 좋아집니다)
    @Transactional(readOnly = true)
    public void login(LoginRequestDto requestDto, HttpSession session) {

        // 1. 이메일로 유저 찾기 (없으면 에러 발생)
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 비밀번호 확인 (다르면 에러 발생)
        if (!user.getPassword().equals(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 비밀번호까지 맞으면 출입증 발급!
        // 아까 만들어둔 SessionConst.LOGIN_USER 이름으로, user 객체 전체를 세션에 저장합니다.
        session.setAttribute(SessionConst.LOGIN_USER, user);
    }

    // 로그아웃 로직 (세션 파기)
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate(); // 현재 세션을 무효화(삭제) 시킵니다.
        }
    }
}