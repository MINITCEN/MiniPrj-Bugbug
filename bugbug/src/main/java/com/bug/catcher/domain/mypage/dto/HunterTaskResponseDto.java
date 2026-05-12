package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.Application;
import lombok.Getter;

@Getter
public class HunterTaskResponseDto {//수행한 의뢰 목록용
    private Long requestId;
    private String title;
    private String status;         // 대기중, 진행중, 완료 등
    private String approxLocation; // 대략적인 주소

    public HunterTaskResponseDto(Application application) {
        this.requestId = application.getRequest().getId();
        this.title = application.getRequest().getTitle();
        this.status = application.getRequest().getStatus();
        this.approxLocation = application.getRequest().getApproxLocation();
    }
}