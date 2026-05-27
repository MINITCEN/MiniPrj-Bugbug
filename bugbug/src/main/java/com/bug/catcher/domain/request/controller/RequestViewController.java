package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.request.dto.RequestDetailResponseDto;
import com.bug.catcher.domain.request.dto.RequestEditFormDto;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.dto.RequestMediaFileUrlDto;
import com.bug.catcher.domain.request.service.RequestService;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import com.bug.catcher.global.common.TimeAgoFormatter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requestView")
@RequiredArgsConstructor
public class RequestViewController {

    private final RequestService requestService;

    @Value("${kakao.api.key}")
    private String kakaoMapApiKey;

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        return "redirect:/requestView/list?error=forbidden";
    }

    @GetMapping("/list")
    public String requestList(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(defaultValue = "latest") String sortType,
            Model model) {

        Sort sort = "viewCount".equals(sortType)
                ? Sort.by(Sort.Direction.DESC, "viewCount")
                : Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<Map<String, Object>> requestPage =
                requestService.readRequestPage(status, sortedPageable);

        if (loginUser != null) {
            model.addAttribute("role", loginUser.getRole());
        }

        model.addAttribute("sortType", sortType);
        model.addAttribute("status", status);
        model.addAttribute("requestPage", requestPage);
        
        // 각 의뢰 데이터에 공통 시간 계산 유틸리티를 적용하여 상대 시간(timeAgo)을 계산 및 주입합니다.
        List<Map<String, Object>> requestListWithTimeAgo = requestPage.getContent().stream().map(req -> {
            Map<String, Object> newReq = new HashMap<>(req);
            LocalDateTime createdAt = (LocalDateTime) req.get("createdAt");
            newReq.put("timeAgo", TimeAgoFormatter.format(createdAt));
            return newReq;
        }).toList();
        
        model.addAttribute("requestList", requestListWithTimeAgo);

        return "wholeRequestList";
    }

    @GetMapping("/new")
    public String requestForm(@AuthenticationPrincipal CustomUserPrincipal loginUser, Model model) {
        if (loginUser == null) {
            return "redirect:/login";
        }
        if (!"USER".equals(loginUser.getRole())) {
            return "redirect:/requestView/list?error=forbidden";
        }
        model.addAttribute("mode", "create");
        model.addAttribute("form", new RequestFormDto());
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);
        return "requestForm";
    }

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRequest(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @ModelAttribute RequestFormDto form) {

        if (loginUser == null) {
            return "redirect:/login";
        }
        requestService.createRequest(loginUser.getUserId(), form);
        return "redirect:/requestView/list";
    }

    @GetMapping("/detail/{requestId}")
    public String requestDetail(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId,
            Model model) {

        RequestDetailResponseDto request = requestService.readRequestDetail(requestId);
        String role = loginUser != null ? loginUser.getRole() : null;
        boolean editable = loginUser != null
                && request.getUserId().equals(loginUser.getUserId())
                && "USER".equals(loginUser.getRole());

        model.addAttribute("request", request);
        model.addAttribute("role", role);
        model.addAttribute("editable", editable);
        model.addAttribute("loginUserId", loginUser != null ? loginUser.getUserId() : null);
        model.addAttribute("loginUserNickname", loginUser != null ? loginUser.getNickname() : null);
        model.addAttribute("liked", false);
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);

        return "requestDetail";
    }

    @GetMapping("/edit/{requestId}")
    public String editForm(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId,
            Model model) {

        // 비로그인 체크
        if (loginUser == null) {
            return "redirect:/login";
        }
        RequestEditFormDto editForm = requestService.getEditForm(requestId, loginUser.getUserId());

        model.addAttribute("mode", "edit");
        model.addAttribute("requestId", requestId);
        model.addAttribute("form", editForm.getForm());
        model.addAttribute("mediaUrl", editForm.getMediaUrl());
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);

        return "requestForm";
    }

    @PostMapping(value = "/edit/{requestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateRequest(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId,
            @ModelAttribute RequestFormDto form,
            @ModelAttribute RequestMediaFileUrlDto mediaUrlDto) {

        // 비로그인 상태 방지
        if (loginUser == null) {
            return "redirect:/login";
        }
        requestService.updateRequest(requestId, loginUser.getUserId(), form, mediaUrlDto);
        return "redirect:/requestView/detail/" + requestId;
    }

    @PostMapping("/remove/{requestId}")
    public String deleteRequest(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId) {

        // 비로그인 상태 방지
        if (loginUser == null) {
            return "redirect:/login";
        }
        requestService.deleteRequest(requestId, loginUser.getUserId());
        return "redirect:/requestView/list";
    }
}
