package com.bug.catcher.domain.citest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AiTestController {

    // 1. 데이터 가공 로직이 들어가는데 @Transactional이 누락된 상황을 모사
    // 2. 가독성이 무너진 3중 중첩 if-else 구조 (AI의 Early Return 리팩토링 유도용)
    @GetMapping("/test/ai-review")
    public String testAiReview() {
        List<String> mockData = new ArrayList<>();
        mockData.add("bugbug-test");
        mockData.add("clean-code");

        // 시니어 개발자 혈압 오르게 만드는 3중 중첩 if 문
        for (String data : mockData) {
            if (data != null) {
                if (data.startsWith("bugbug")) {
                    if (data.equals("bugbug-test")) {
                        System.out.println("AI 피드백 타겟 텍스트: " + data);
                    }
                }
            }
        }

        return "AI 코드 리뷰 테스트 파일 정상 구동";
    }
}