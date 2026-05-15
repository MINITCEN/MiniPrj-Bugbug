package com.bug.catcher.domain.request.controller;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.service.RequestService;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/requestView")
@RequiredArgsConstructor
public class RequestViewController {

    private final RequestService requestService;

    @Value("${kakao.api.key}")
    private String kakaoMapApiKey;

    @GetMapping("/list")
    public String requestList(HttpSession session, Model model) {
        List<Map<String, Object>> requestList = requestService.readRequestList();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("role", user.getRole());
        }
        model.addAttribute("requestList", requestList);
        return "wholeRequestList";
    }

    @GetMapping("/new")
    public String requestForm(Model model) {
        model.addAttribute("kakaoMapKey", kakaoMapApiKey);
        return "requestForm";
    }

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRequest(
            @ModelAttribute RequestFormDto form,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "redirect:/api/users/signup";
            //throw new IllegalStateException("로그인이 필요합니다.");
        }

        requestService.createRequest(loginUser.getId(), form);
        return "redirect:/api/requestView/list";
    }
}
