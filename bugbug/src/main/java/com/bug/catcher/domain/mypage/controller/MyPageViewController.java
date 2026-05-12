package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.global.auth.SessionConst;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/mypage")
public class MyPageViewController {

    @GetMapping("/dashboard")
    public String dashboardView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            Model model) {

        // 로그인 안 한 상태면 로그인 페이지로 가기
        if (loginUser == null) {
            return "redirect:/login";
        }

        // 타임리프에서 쓸 수 있도록 유저 정보 전달 (이름, 권한 등)
        model.addAttribute("user", loginUser);
        return "dashboard";
    }
}