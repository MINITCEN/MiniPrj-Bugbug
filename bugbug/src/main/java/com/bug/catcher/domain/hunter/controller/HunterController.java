package com.bug.catcher.domain.hunter.controller;

import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.hunter.service.HunterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}