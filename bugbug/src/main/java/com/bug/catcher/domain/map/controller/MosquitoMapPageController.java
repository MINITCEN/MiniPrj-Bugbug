package com.bug.catcher.domain.map.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MosquitoMapPageController {

    @GetMapping("/mosquito-map")
    public String mosquitoMapPage() {
        return "mosquito-map";
    }
}
