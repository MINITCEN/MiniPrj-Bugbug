package com.bug.catcher.domain.hunter.controller;

import com.bug.catcher.domain.hunter.service.SavedRequestService;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hunters/requests")
@RequiredArgsConstructor
public class SavedRequestController {
    private final SavedRequestService savedRequestService;

    @PreAuthorize("hasRole('HUNTER')")
    @PostMapping("/{requestId}/bookmarks")
    public ResponseEntity<Map<String, Object>> toggleSavedRequest(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId
    ) {
        boolean bookmarked = savedRequestService.toggleSavedRequest(
                loginUser.getUserId(),
                requestId
        );
        return ResponseEntity.ok(Map.of(
                "bookmarked", bookmarked,
                "message", bookmarked ? "의뢰를 찜했습니다." : "의뢰 찜을 취소했습니다."
        ));
    }
}
