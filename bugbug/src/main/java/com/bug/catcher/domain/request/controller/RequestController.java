package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.dto.RequestMediaFileUrlDto;
import jakarta.servlet.http.HttpSession;
import com.bug.catcher.global.auth.SessionConst;
import org.springframework.http.MediaType;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.dto.RequestDetailResponseDto;
import com.bug.catcher.domain.request.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/request")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    //create request
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<Map<String, Object>> createRequest(
            @ModelAttribute RequestFormDto form,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        requestService.createRequest(loginUser.getId(), form);
        System.out.println("등록 성공");
        return requestService.readRequestList();
    }

    //read request(전체 게시판)
    @GetMapping(value = "/wholeList")
    public List<Map<String, Object>> readRequestList() {
        System.out.println("조회 성공");
        return requestService.readRequestList();
    }

    // 상세보기
    @GetMapping(value = "/detail/{id}")
    public RequestDetailResponseDto requestDetail(@PathVariable Long id) {
        return requestService.readRequestDetail(id);
    }

    // update request
    @PatchMapping(value = "/edit/{requestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RequestDetailResponseDto updateRequest(@PathVariable Long requestId, @ModelAttribute RequestFormDto form, @ModelAttribute RequestMediaFileUrlDto mediaUrlDto, HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        requestService.updateRequest(requestId, loginUser.getId(), form, mediaUrlDto);
        System.out.println("수정 성공");
        return requestService.readRequestDetail(requestId);
    }

    // delete request
    @DeleteMapping(value = "/remove/{requestId}")
    public List<Map<String, Object>> deleteRequestList(@PathVariable Long requestId, HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        requestService.deleteRequest(requestId, loginUser.getId());
        return requestService.readRequestList();
    }
}