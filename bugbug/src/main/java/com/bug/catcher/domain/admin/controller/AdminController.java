package com.bug.catcher.domain.admin.controller;

import com.bug.catcher.domain.admin.dto.AdminUserResponseDto;
import com.bug.catcher.domain.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin API", description = "관리자 전용 기능 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "전체 회원 조회", description = "전체 유저 및 헌터 목록을 페이징하여 조회합니다.")
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserResponseDto>> getUsers(
            @RequestParam(required = false) String role,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable) {
        
        Page<AdminUserResponseDto> response = adminService.getUsers(role, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원 정지 처리 (소프트 삭제)", description = "유저의 계정을 특정 기간 또는 영구 정지(SUSPENDED) 처리합니다.")
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<Void> suspendUser(
            @PathVariable("userId") Long userId,
            @RequestParam("days") int days) {
        // days = 7 (7일 정지), days = -1 (영구 정지)
        adminService.suspendUser(userId, days);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "헌터 승인 대기 목록 조회", description = "승인 대기 중인 헌터 신청서 목록을 페이징하여 조회합니다.")
    @GetMapping("/applications/pending")
    public ResponseEntity<Page<com.bug.catcher.domain.admin.dto.AdminApplicationResponseDto>> getPendingApplications(
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable) {
        
        Page<com.bug.catcher.domain.admin.dto.AdminApplicationResponseDto> response = adminService.getPendingApplications(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "헌터 승인 처리", description = "헌터 신청을 한 유저를 최종 승인하여 HUNTER 권한을 부여합니다.")
    @PostMapping("/applications/{applicationId}/approve") // URL도 신청서 id를 받는 것으로 변경
    public ResponseEntity<Void> approveHunter(@PathVariable("applicationId") Long applicationId) {
        // [수정됨] 서비스 메서드 이름과 일치시킴
        adminService.approveHunterApplication(applicationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "헌터 신청 거절 처리", description = "헌터 신청을 거절하여 상태를 REJECTED로 변경합니다.")
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<Void> rejectHunter(@PathVariable("applicationId") Long applicationId) {
        adminService.rejectHunterApplication(applicationId);
        return ResponseEntity.ok().build();
    }
}
