package com.bug.catcher.domain.user.controller;

import com.bug.catcher.domain.user.dto.SignupRequestDto;
import com.bug.catcher.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto requestDto) {
        // 1. DTO을 받아서 Service에게 전달합니다.
        userService.signup(requestDto);

        // 2. 문제 없이 저장이 완료되면 성공 메시지와 201(Created) 상태 코드를 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }
}