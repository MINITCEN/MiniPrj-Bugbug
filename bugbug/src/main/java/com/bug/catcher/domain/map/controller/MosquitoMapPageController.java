package com.bug.catcher.domain.map.controller;

import com.bug.catcher.domain.map.service.MosquitoMapQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class MosquitoMapPageController {

    private final MosquitoMapQueryService mosquitoMapQueryService;

    @GetMapping("/mosquito-map")
    public String mosquitoMapPage(Model model) {
        model.addAttribute("pageData", mosquitoMapQueryService.getPageData());
        return "mosquito-map";
    }
}
