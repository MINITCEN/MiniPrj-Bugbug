package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.mypage.dto.*;
import com.bug.catcher.domain.mypage.service.MyPageService;
import com.bug.catcher.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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
    // 나의 의뢰 목록 조회
    @GetMapping("/requests")
    public ResponseEntity<List<MyRequestResponseDto>> getMyRequests(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        List<MyRequestResponseDto> response = myPageService.getMyRequests(loginUser.getId());
        return ResponseEntity.ok(response);
    }

    // 찜한 헌터 목록 조회
    @GetMapping("/bookmarks/hunters")
    public ResponseEntity<List<MySavedHunterResponseDto>> getMySavedHunters(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        List<MySavedHunterResponseDto> response = myPageService.getMySavedHunters(loginUser.getId());
        return ResponseEntity.ok(response);
    }

    // 리뷰 작성
    @PostMapping("/reviews")
    public ResponseEntity<String> createReview(
            @RequestBody ReviewCreateRequestDto requestDto,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        myPageService.createReview(loginUser.getId(), requestDto);
        return ResponseEntity.ok("리뷰가 성공적으로 등록되었습니다.");
    }

    // 리뷰 수정
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        myPageService.updateReview(loginUser.getId(), reviewId, requestDto);
        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    // 리뷰 삭제
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        myPageService.deleteReview(loginUser.getId(), reviewId);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getMyPageDashboard(HttpServletRequest request) {
        // 1. 현재 세션에 저장된 유저 정보 가져오기
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        // 2. DB에서 가장 최신의 유저 정보 조회
        User dbUser = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 3. 세션의 권한과 DB의 권한이 다르다면? (관리자가 그 사이에 승인해준 경우)
        if (!sessionUser.getRole().equals(dbUser.getRole())) {
            // 세션 정보를 최신 DB 정보로 덮어씌움 (새로고침이나 재로그인 없이 즉시 헌터 권한 적용)
            session.setAttribute(SessionConst.LOGIN_USER, dbUser);
            sessionUser = dbUser; // 현재 요청에서도 헌터 정보를 쓰기 위해 동기화
        }

        // 이후 sessionUser.getRole()이 "HUNTER"인지 분기하여 헌터용 데이터 반환
        // ...
    }
}
