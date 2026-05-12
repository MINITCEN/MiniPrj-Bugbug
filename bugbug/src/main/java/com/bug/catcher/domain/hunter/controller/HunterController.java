package com.bug.catcher.domain.hunter.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.hunter.service.HunterService;
import com.bug.catcher.global.auth.SessionConst;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import com.bug.catcher.domain.hunter.dto.HunterListResponseDto;

@RestController
@RequestMapping("/api/hunters")
@RequiredArgsConstructor
public class HunterController {

    private final HunterService hunterService;

    // 헌터의 공개 프로필 조회 (로그인 안 한 유저도 볼 수 있음)
    @GetMapping("/{hunterId}/profile")
    public ResponseEntity<HunterProfileResponseDto> getHunterProfile(@PathVariable Long hunterId) {
        HunterProfileResponseDto response = hunterService.getHunterProfile(hunterId);
        return ResponseEntity.ok(response);
    }
    // 헌터 찜하기 / 찜 취소 토글
    @PostMapping("/{hunterId}/bookmarks")
    public ResponseEntity<String> toggleSavedHunter(
            @PathVariable Long hunterId,
            @SessionAttribute(SessionConst.LOGIN_USER) User loginUser) {

        String message = hunterService.toggleSavedHunter(loginUser.getId(), hunterId);
        return ResponseEntity.ok(message);
    }
    // 전체 헌터 목록 조회 API
    @GetMapping
    public ResponseEntity<Page<HunterListResponseDto>> getHunters(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @SessionAttribute(SessionConst.LOGIN_USER) User loginUser) {

        Page<HunterListResponseDto> response = hunterService.getHunterList(loginUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }
}