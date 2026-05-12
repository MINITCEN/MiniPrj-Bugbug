package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageViewController {

    private final UserRepository userRepository;

    @GetMapping
    public String myPage() {
        return "redirect:/mypage/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboardView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser,
            HttpServletRequest request,
            Model model) {

        if (loginUser == null) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findById(loginUser.getId())
                .orElse(loginUser);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SessionConst.LOGIN_USER, currentUser);
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("isHunter", "HUNTER".equals(currentUser.getRole()));
        return "dashboard";
    }

    @GetMapping("/requests")
    public String requestListView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser) {
        if (loginUser == null) return "redirect:/login";
        return "request-list";
    }

    @GetMapping("/reviews")
    public String reviewListView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser) {
        if (loginUser == null) return "redirect:/login";
        return "review-list";
    }

    @GetMapping("/bookmarks/hunters")
    public String bookmarkListView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser) {
        if (loginUser == null) return "redirect:/login";
        return "bookmark-list";
    }

    @GetMapping("/hunter/tasks")
    public String hunterTaskListView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser) {
        if (loginUser == null) return "redirect:/login";
        return "hunter-task-list";
    }

    @GetMapping("/hunter/bookmarks/requests")
    public String hunterBookmarkListView(
            @SessionAttribute(name = SessionConst.LOGIN_USER, required = false) User loginUser) {
        if (loginUser == null) return "redirect:/login";
        return "hunter-bookmark-list";
    }
}
