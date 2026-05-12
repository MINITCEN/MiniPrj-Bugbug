package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.service.HunterService;
import com.bug.catcher.domain.mypage.dto.*;
import com.bug.catcher.domain.mypage.service.MyPageService;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

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

    // 헌터 신청 팝업에서 제출 버튼을 눌렀을 때 호출
    @PostMapping("/hunter/apply")
    public ResponseEntity<String> applyForHunter(@RequestBody HunterApplyRequestDto requestDto, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        myPageService.applyForHunter(loginUser.getId(), requestDto);
        return ResponseEntity.ok("헌터 신청이 성공적으로 접수되었습니다.");
    }

    // 마이페이지 대시보드 접근 시 세션/권한 동기화
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getMyPageDashboard(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User sessionUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        User dbUser = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (!sessionUser.getRole().equals(dbUser.getRole())) {
            session.setAttribute(SessionConst.LOGIN_USER, dbUser);
            sessionUser = dbUser;
        }

        DashboardResponseDto responseDto = new DashboardResponseDto(sessionUser.getRole(), sessionUser.getNickname());
        return ResponseEntity.ok(responseDto);
    }
    //이슈 4
    // 수행한 의뢰 목록 조회 (헌터 전용) - 페이징 추가
    @GetMapping("/hunter/tasks")
    public ResponseEntity<Page<HunterTaskResponseDto>> getHunterTasks(
            @SessionAttribute(SessionConst.LOGIN_USER) User loginUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HunterTaskResponseDto> response = myPageService.getHunterTasks(loginUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    // 찜한 게시물 목록 조회 (헌터 전용) - 페이징 추가
    @GetMapping("/hunter/bookmarks/requests")
    public ResponseEntity<Page<HunterSavedRequestDto>> getHunterSavedRequests(
            @SessionAttribute(SessionConst.LOGIN_USER) User loginUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HunterSavedRequestDto> response = myPageService.getHunterSavedRequests(loginUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    // 헌터 등록 해제 (일반 유저로 돌아가기)
    @PostMapping("/hunter/resign")
    public ResponseEntity<String> resignHunter(
            @SessionAttribute(SessionConst.LOGIN_USER) User loginUser,
            HttpServletRequest request) {

        // 1. DB의 Role 강등 처리
        myPageService.resignHunter(loginUser.getId());

        // 2. 세션 동기화 (즉시 일반 유저 마이페이지로 바뀌도록 현재 세션도 갱신)
        User dbUser = userRepository.findById(loginUser.getId()).orElseThrow();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SessionConst.LOGIN_USER, dbUser);
        }

        return ResponseEntity.ok("헌터 등록이 성공적으로 해제되었습니다.");
    }
}
