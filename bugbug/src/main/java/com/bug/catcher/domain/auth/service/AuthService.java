package com.bug.catcher.domain.auth.service;

import com.bug.catcher.domain.auth.dto.LoginRequestDto;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import com.bug.catcher.domain.entity.AccountStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository; // Auth 도메인이지만, 유저 정보를 확인해야 하므로 User 도메인의 리포지토리를 가져옴
    private final PasswordEncoder passwordEncoder; // 주입받기

    @Transactional // 계정 복구(activate) 시 상태 저장을 위해 readOnly 속성 제거
    public void login(LoginRequestDto requestDto, HttpSession session) {

        // 1. 이메일로 유저 찾기
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        // 2. 계정 정지 여부 확인
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            if (user.getBanEndDate() != null && LocalDateTime.now().isAfter(user.getBanEndDate())) {
                // 정지 기간이 지났으면 계정 자동 복구
                user.activate();
            } else {
                // 아직 정지 기간이면 로그인 차단
                String endDateStr = user.getBanEndDate() != null 
                        ? user.getBanEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) 
                        : "영구";
                throw new IllegalArgumentException("정지된 계정입니다. (정지 해제 일시: " + endDateStr + ")");
            }
        }

        // 3. 비밀번호 확인 (입력된 평문 비밀번호와 DB의 암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 4. 비밀번호까지 맞으면 출입증 발급
        // 아까 만들어둔 SessionConst.LOGIN_USER 이름으로, user 객체 전체를 세션에 저장.
        session.setAttribute(SessionConst.LOGIN_USER, user);
    }

    // 로그아웃 로직
    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate(); // 현재 세션을 무효화.
        }
    }
}