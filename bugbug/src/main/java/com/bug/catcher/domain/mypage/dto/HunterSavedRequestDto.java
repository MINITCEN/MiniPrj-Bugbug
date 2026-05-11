package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.SavedRequest;
import lombok.Getter;

@Getter
public class HunterSavedRequestDto {//찜한 의뢰목록용
    private Long requestId;
    private String title;
    private String approxLocation;

    public HunterSavedRequestDto(SavedRequest savedRequest) {
        this.requestId = savedRequest.getRequest().getId();
        this.title = savedRequest.getRequest().getTitle();
        this.approxLocation = savedRequest.getRequest().getApproxLocation();
    }
}