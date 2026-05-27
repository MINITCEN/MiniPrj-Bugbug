package com.bug.catcher.domain.main.controller;

import com.bug.catcher.domain.request.service.RequestService;
import com.bug.catcher.global.common.TimeAgoFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 메인 페이지 및 서비스 소개 페이지를 매핑하는 컨트롤러 클래스입니다.
 */
@Controller
@RequiredArgsConstructor
public class MainPageController {

    // 의뢰글 조회를 위한 서비스 클래스 의존성 주입
    private final RequestService requestService;

    /**
     * 메인 홈 화면(/)을 렌더링합니다.
     * 최근 등록된 실제 방역 의뢰글 4건을 내림차순으로 조회하여 상대 시간("N분 전")과 함께 뷰에 전달합니다.
     */
    @GetMapping("/")
    public String mainPage(Model model) {
        // 최신 생성일(createdAt) 기준으로 내림차순 정렬하여 최대 4건(페이지 0, 사이즈 4)을 페이징 조회
        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Map<String, Object>> requestPage = requestService.readRequestPage(null, pageable);
        
        // 각 의뢰글 정보(Map)에 경과 시간 포맷(timeAgo)을 계산하여 주입
        List<Map<String, Object>> recentRequests = requestPage.getContent().stream().map(req -> {
            Map<String, Object> newReq = new HashMap<>(req);
            LocalDateTime createdAt = (LocalDateTime) req.get("createdAt");
            
            // 전역 공통 시간 포맷터를 호출하여 "방금 전", "N분 전" 등으로 텍스트 계산 후 추가
            newReq.put("timeAgo", TimeAgoFormatter.format(createdAt));
            return newReq;
        }).toList();

        // 뷰(main.html) 템플릿에서 꺼내 쓸 수 있도록 모델에 데이터 추가
        model.addAttribute("recentRequests", recentRequests);
        return "main";
    }

    /**
     * 서비스 소개 페이지(/service-intro)를 렌더링합니다.
     */
    @GetMapping("/service-intro")
    public String serviceIntroPage() {
        return "service-intro";
    }
}
