package com.bug.catcher.domain.hunter.controller;

import com.bug.catcher.domain.hunter.dto.HunterListResponseDto;
import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.hunter.service.HunterService;
import com.bug.catcher.domain.review.dto.ReviewResponseDto;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hunters")
@RequiredArgsConstructor
public class HunterController {

    private final HunterService hunterService;

    @GetMapping("/{hunterId}/profile")
    public ResponseEntity<HunterProfileResponseDto> getHunterProfile(@PathVariable Long hunterId) {
        HunterProfileResponseDto response = hunterService.getHunterProfile(hunterId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{hunterId}/bookmarks")
    public ResponseEntity<String> toggleSavedHunter(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long hunterId) {

        String message = hunterService.toggleSavedHunter(loginUser.getUserId(), hunterId);
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<Page<HunterListResponseDto>> getHunters(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Long loginUserId = loginUser != null ? loginUser.getUserId() : null;
        Page<HunterListResponseDto> response = hunterService.getHunterList(loginUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hunterId}/reviews")
    public ResponseEntity<Page<ReviewResponseDto>> getHunterReviews(
            @PathVariable Long hunterId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ReviewResponseDto> response = hunterService.getHunterReviews(hunterId, pageable);
        return ResponseEntity.ok(response);
    }
}
