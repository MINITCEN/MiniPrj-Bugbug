package com.bug.catcher.domain.auth.controller;

import com.bug.catcher.domain.auth.dto.LoginRequestDto;
import com.bug.catcher.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<String> login(
            @RequestBody LoginRequestDto requestDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        authService.login(requestDto, request, response);
        return ResponseEntity.ok("로그인에 성공했습니다.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
