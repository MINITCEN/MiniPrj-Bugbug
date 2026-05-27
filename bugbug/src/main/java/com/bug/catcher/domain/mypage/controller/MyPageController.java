package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.mypage.dto.DashboardResponseDto;
import com.bug.catcher.domain.mypage.dto.HunterApplyRequestDto;
import com.bug.catcher.domain.mypage.dto.HunterSavedRequestDto;
import com.bug.catcher.domain.mypage.dto.HunterTaskResponseDto;
import com.bug.catcher.domain.mypage.dto.MyInfoResponseDto;
import com.bug.catcher.domain.mypage.dto.MyInfoUpdateRequestDto;
import com.bug.catcher.domain.mypage.dto.MyRequestResponseDto;
import com.bug.catcher.domain.mypage.dto.MySavedHunterResponseDto;
import com.bug.catcher.domain.mypage.dto.ReviewCreateRequestDto;
import com.bug.catcher.domain.mypage.dto.ReviewUpdateRequestDto;
import com.bug.catcher.domain.mypage.service.MyPageService;
import com.bug.catcher.domain.review.dto.ReviewResponseDto;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final UserRepository userRepository;

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @GetMapping("/info")
    public ResponseEntity<MyInfoResponseDto> getMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal loginUser) {

        MyInfoResponseDto responseDto = myPageService.getMyInfo(loginUser.getUserId());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @PatchMapping("/info")
    public ResponseEntity<MyInfoResponseDto> updateMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @RequestBody MyInfoUpdateRequestDto requestDto) {

        MyInfoResponseDto responseDto = myPageService.updateMyInfo(loginUser.getUserId(), requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/requests")
    public ResponseEntity<Page<MyRequestResponseDto>> getMyRequests(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<MyRequestResponseDto> response = myPageService.getMyRequests(loginUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/bookmarks/hunters")
    public ResponseEntity<Page<MySavedHunterResponseDto>> getMySavedHunters(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<MySavedHunterResponseDto> response = myPageService.getMySavedHunters(loginUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/reviews")
    public ResponseEntity<String> createReview(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @RequestBody ReviewCreateRequestDto requestDto) {

        myPageService.createReview(loginUser.getUserId(), requestDto);
        return ResponseEntity.ok("리뷰가 성공적으로 등록되었습니다.");
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<String> updateReview(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long reviewId,
            @RequestBody ReviewUpdateRequestDto requestDto) {

        myPageService.updateReview(loginUser.getUserId(), reviewId, requestDto);
        return ResponseEntity.ok("리뷰가 성공적으로 수정되었습니다.");
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long reviewId) {

        myPageService.deleteReview(loginUser.getUserId(), reviewId);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getMyReviews(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewResponseDto> response = myPageService.getMyReviews(loginUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/hunter/apply")
    public ResponseEntity<String> applyForHunter(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @RequestBody HunterApplyRequestDto requestDto) {

        myPageService.applyForHunter(loginUser.getUserId(), requestDto);
        return ResponseEntity.ok("헌터 신청이 성공적으로 접수되었습니다.");
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponseDto> getMyPageDashboard(
            @AuthenticationPrincipal CustomUserPrincipal loginUser) {

        User dbUser = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DashboardResponseDto responseDto = new DashboardResponseDto(dbUser.getRole(), dbUser.getNickname());
        return ResponseEntity.ok(responseDto);
    }

    @PreAuthorize("hasRole('HUNTER')")
    @GetMapping("/hunter/tasks")
    public ResponseEntity<Page<HunterTaskResponseDto>> getHunterTasks(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HunterTaskResponseDto> response = myPageService.getHunterTasks(loginUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('HUNTER')")
    @GetMapping("/hunter/bookmarks/requests")
    public ResponseEntity<Page<HunterSavedRequestDto>> getHunterSavedRequests(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HunterSavedRequestDto> response = myPageService.getHunterSavedRequests(loginUser.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('HUNTER')")
    @PostMapping("/hunter/resign")
    public ResponseEntity<String> resignHunter(
            @AuthenticationPrincipal CustomUserPrincipal loginUser) {

        myPageService.resignHunter(loginUser.getUserId());
        return ResponseEntity.ok("헌터 등록이 성공적으로 해제되었습니다.");
    }

    @PreAuthorize("hasRole('HUNTER')")
    @GetMapping("/hunter/profile")
    public ResponseEntity<HunterProfileResponseDto> getMyHunterProfile(
            @AuthenticationPrincipal CustomUserPrincipal loginUser) {

        HunterProfileResponseDto response = myPageService.getMyHunterProfile(loginUser.getUserId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('HUNTER')")
    @GetMapping("/hunter/review-summary")
    public ResponseEntity<Map<Integer, Long>> getMyHunterReviewSummary(
            @AuthenticationPrincipal CustomUserPrincipal loginUser) {

        Map<Integer, Long> response = myPageService.getMyHunterReviewSummary(loginUser.getUserId());
        return ResponseEntity.ok(response);
    }
}
