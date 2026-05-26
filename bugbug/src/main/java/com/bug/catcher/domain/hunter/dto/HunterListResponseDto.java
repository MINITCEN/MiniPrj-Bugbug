package com.bug.catcher.domain.hunter.dto;

import com.bug.catcher.domain.entity.Hunter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class HunterListResponseDto {
    private Long hunterId;
    private String name;
    private String grade;
    private long completionCount;
    private long responseCount;

    // 현재 로그인한 유저가 이 헌터를 찜했는지 여부
    @JsonProperty("isBookmarked")
    private boolean isBookmarked;

    public HunterListResponseDto(Hunter hunter, boolean isBookmarked, long completionCount) {
        this.hunterId = hunter.getId();
        this.name = hunter.getName();
        this.grade = hunter.getGrade();
        this.completionCount = completionCount;
        this.responseCount = completionCount;
        this.isBookmarked = isBookmarked;
    }
}
