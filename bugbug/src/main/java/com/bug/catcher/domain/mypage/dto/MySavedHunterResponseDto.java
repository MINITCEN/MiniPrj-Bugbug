package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.SavedHunter;
import lombok.Getter;

@Getter
public class MySavedHunterResponseDto {//찜한 헌터 목록용
    private Long hunterId;
    private String hunterName;
    private String grade; // 헌터 등급
    private Integer responseCount; // 헌터의 완료 횟수

    public MySavedHunterResponseDto(SavedHunter savedHunter) {
        this.hunterId = savedHunter.getHunter().getId();
        this.hunterName = savedHunter.getHunter().getName();
        this.grade = savedHunter.getHunter().getGrade();
        this.responseCount = savedHunter.getHunter().getResponseCount();
    }
}
