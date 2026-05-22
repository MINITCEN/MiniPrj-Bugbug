package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.dto.RequestDetailResponseDto;
import com.bug.catcher.domain.request.dto.RequestEditFormDto;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.dto.RequestMediaFileUrlDto;
import com.bug.catcher.domain.request.service.RequestService;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/requestView")
@RequiredArgsConstructor
public class RequestViewController {

    private final RequestService requestService;

    @Value("${kakao.api.key}")
    private String kakaoMapApiKey;


    // 전체 게시판 조회하기
    @GetMapping("/list")
    public String requestList(@PageableDefault(size=10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, HttpSession session, Model model) {
        Page<Map<String, Object>> requestPage = requestService.readRequestPage(pageable);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("role", user.getRole());
        }

        model.addAttribute("requestPage", requestPage);
        model.addAttribute("requestList", requestPage.getContent());
        return "wholeRequestList";
    }

    // 의뢰 등록 화면
    @GetMapping("/new")
    public String requestForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("form", new RequestFormDto());
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);
        return "requestForm";
    }

    // 의뢰 등록 처리
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRequest(@ModelAttribute RequestFormDto form, HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "login";
        }

        requestService.createRequest(loginUser.getId(), form);
        return "redirect:/api/requestView/list";
    }

    // 의뢰 상세 화면
    @GetMapping("/detail/{requestId}")
    public String requestDetail(@PathVariable Long requestId, HttpSession session, Model model) {
        RequestDetailResponseDto request = requestService.readRequestDetail(requestId);
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        String role = null;
        boolean editable = false;

        if (loginUser != null) {
            role = loginUser.getRole();

            // 작성자인 의뢰인만 수정/삭제 가능
            editable = request.getUserId().equals(loginUser.getId()) && "USER".equals(loginUser.getRole());
        }
        model.addAttribute("request", request);
        model.addAttribute("role", role);
        model.addAttribute("editable", editable);
        model.addAttribute("loginUserId", loginUser != null ? loginUser.getId() : null);
        model.addAttribute("loginUserNickname", loginUser != null ? loginUser.getNickname() : null);
        model.addAttribute("liked", false); // 찜 기능 구현 전 임시값
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);

        return "requestDetail";
    }

    // 의뢰 수정 화면
    @GetMapping("/edit/{requestId}")
    public String editForm(@PathVariable Long requestId, HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        RequestEditFormDto editForm = requestService.getEditForm(requestId, loginUser.getId());

        model.addAttribute("mode", "edit");
        model.addAttribute("requestId", requestId);
        model.addAttribute("form", editForm.getForm());
        model.addAttribute("mediaUrl", editForm.getMediaUrl());
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);

        return "requestForm";
    }

    // 의뢰 수정 처리
    @PostMapping(value = "/edit/{requestId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateRequest(@PathVariable Long requestId, @ModelAttribute RequestFormDto form, @ModelAttribute RequestMediaFileUrlDto mediaUrlDto, HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            return "redirect:/login";
        }

        requestService.updateRequest(requestId, loginUser.getId(), form, mediaUrlDto);
        return "redirect:/api/requestView/detail/" + requestId;
    }

    // 의뢰 삭제 처리
    @PostMapping("/remove/{requestId}")
    public String deleteRequest(@PathVariable Long requestId, HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            return "redirect:/login";
        }
        requestService.deleteRequest(requestId, loginUser.getId());
        return "redirect:/api/requestView/list";
    }
}
