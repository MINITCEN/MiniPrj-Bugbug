package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.mypage.dto.MyInfoResponseDto;
import com.bug.catcher.domain.mypage.service.MyPageService;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/info")
    public ResponseEntity<MyInfoResponseDto> getMyInfo(HttpServletRequest request) {

        // 1. 세션에서 현재 로그인한 유저 객체 꺼내기
        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        // 2. 서비스에게 유저 ID를 넘겨서 최신 정보(DTO) 받아오기
        MyInfoResponseDto responseDto = myPageService.getMyInfo(loginUser.getId());

        // 3. 정상적으로 데이터 반환
        return ResponseEntity.ok(responseDto);
    }

}
