package com.bug.catcher.domain.hunter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HunterPageController {

    @GetMapping("/hunter")
    public String hunterPage() {
        return "hunter";
    }
}
