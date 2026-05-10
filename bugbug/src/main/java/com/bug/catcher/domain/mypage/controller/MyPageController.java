package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.mypage.dto.MyInfoResponseDto;
import com.bug.catcher.domain.mypage.dto.MyRequestResponseDto;
import com.bug.catcher.domain.mypage.dto.MySavedHunterResponseDto;
import com.bug.catcher.domain.mypage.dto.ReviewCreateRequestDto;
import com.bug.catcher.domain.mypage.service.MyPageService;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
// 이슈2
// 1. 나의 의뢰 목록 조회
@GetMapping("/requests")
public ResponseEntity<List<MyRequestResponseDto>> getMyRequests(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

    List<MyRequestResponseDto> response = myPageService.getMyRequests(loginUser.getId());
    return ResponseEntity.ok(response);
}

    // 2. 찜한 헌터 목록 조회
    @GetMapping("/bookmarks/hunters")
    public ResponseEntity<List<MySavedHunterResponseDto>> getMySavedHunters(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        List<MySavedHunterResponseDto> response = myPageService.getMySavedHunters(loginUser.getId());
        return ResponseEntity.ok(response);
    }

    // 3. 리뷰 작성
    @PostMapping("/reviews")
    public ResponseEntity<String> createReview(
            @RequestBody ReviewCreateRequestDto requestDto,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        myPageService.createReview(loginUser.getId(), requestDto);
        return ResponseEntity.ok("리뷰가 성공적으로 등록되었습니다.");
    }
}
