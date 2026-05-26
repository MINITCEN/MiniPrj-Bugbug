package com.bug.catcher.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageController {

    @GetMapping("/users")
    public String showUserListPage() {
        return "admin-users";
    }

    @GetMapping("/applications")
    public String showApplicationListPage() {
        return "admin-applications";
    }
}
