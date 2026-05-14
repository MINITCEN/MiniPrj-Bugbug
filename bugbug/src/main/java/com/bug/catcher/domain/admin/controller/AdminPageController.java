package com.bug.catcher.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
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
