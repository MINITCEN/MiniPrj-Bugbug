package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.request.dto.RequestDetailResponseDto;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.dto.RequestMediaFileUrlDto;
import com.bug.catcher.domain.request.service.RequestService;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, Object>> createRequest(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @ModelAttribute RequestFormDto form) {
        requestService.createRequest(loginUser.getUserId(), form);
        return requestService.readRequestList();
    }

    @GetMapping(value = "/wholeList")
    public List<Map<String, Object>> readRequestList() {
        return requestService.readRequestList();
    }

    @GetMapping(value = "/detail/{id}")
    public RequestDetailResponseDto requestDetail(@PathVariable Long id) {
        return requestService.readRequestDetail(id);
    }

    @PreAuthorize("hasRole('USER') and @requestPermissionChecker.isOwner(#requestId, principal.userId)")
    @PatchMapping(value = "/edit/{requestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RequestDetailResponseDto updateRequest(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId,
            @ModelAttribute RequestFormDto form,
            @ModelAttribute RequestMediaFileUrlDto mediaUrlDto) {

        requestService.updateRequest(requestId, loginUser.getUserId(), form, mediaUrlDto);
        return requestService.readRequestDetail(requestId);
    }

    @PreAuthorize("hasRole('USER') and @requestPermissionChecker.isOwner(#requestId, principal.userId)")
    @DeleteMapping(value = "/remove/{requestId}")
    public List<Map<String, Object>> deleteRequestList(
            @PathVariable Long requestId) {
        requestService.deleteRequest(requestId);
        return requestService.readRequestList();
    }
}
