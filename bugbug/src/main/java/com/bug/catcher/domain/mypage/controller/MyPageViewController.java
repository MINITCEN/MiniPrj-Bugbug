package com.bug.catcher.domain.mypage.controller;

import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            Model model) {

        User currentUser = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        model.addAttribute("user", currentUser);
        model.addAttribute("isHunter", "HUNTER".equals(currentUser.getRole()));
        return "dashboard";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/requests")
    public String requestListView() {
        return "request-list";
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @GetMapping("/reviews")
    public String reviewListView() {
        return "review-list";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/bookmarks/hunters")
    public String bookmarkListView() {
        return "bookmark-list";
    }

    @PreAuthorize("hasRole('HUNTER')")
    @GetMapping("/hunter/tasks")
    public String hunterTaskListView() {
        return "hunter-task-list";
    }

    @PreAuthorize("hasRole('HUNTER')")
    @GetMapping("/hunter/bookmarks/requests")
    public String hunterBookmarkListView() {
        return "hunter-bookmark-list";
    }
}
