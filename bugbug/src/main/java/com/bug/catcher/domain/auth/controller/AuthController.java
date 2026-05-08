package com.bug.catcher.domain.auth.controller;

import com.bug.catcher.domain.auth.dto.LoginRequestDto;
import com.bug.catcher.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto requestDto, HttpServletRequest request) {

        // 1. 세션 준비하기 (true: 기존 세션이 없으면 새로 만들어 줍니다)
        HttpSession session = request.getSession(true);

        // 2. 서비스에게 요리(검증 및 출입증 발급) 맡기기
        authService.login(requestDto, session);

        return ResponseEntity.ok("로그인에 성공했습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        // 1. 현재 세션 가져오기 (false: 기존 세션이 없으면 굳이 새로 만들지 않고 null을 반환합니다)
        HttpSession session = request.getSession(false);

        // 2. 서비스에게 세션 폐기 맡기기
        authService.logout(session);

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}