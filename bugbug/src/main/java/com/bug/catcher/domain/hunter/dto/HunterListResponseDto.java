package com.bug.catcher.domain.hunter.dto;

import com.bug.catcher.domain.entity.Hunter;
import lombok.Getter;

@Getter
public class HunterListResponseDto {
    private Long hunterId;
    private String name;
    private String grade;
    private long requestCount;  // 요청받은 횟수
    private long responseCount; // 응답한 횟수

    // 현재 로그인한 유저가 이 헌터를 찜했는지 여부
    private boolean isBookmarked;

    public HunterListResponseDto(Hunter hunter, boolean isBookmarked) {
        this.hunterId = hunter.getId();
        this.name = hunter.getName();
        this.grade = hunter.getGrade();
        this.requestCount = hunter.getRequestCount() != null ? hunter.getRequestCount() : 0;
        this.responseCount = hunter.getResponseCount() != null ? hunter.getResponseCount() : 0;
        this.isBookmarked = isBookmarked;
    }
}