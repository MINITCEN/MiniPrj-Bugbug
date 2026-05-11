package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.Request;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class MyRequestResponseDto { //나의 의뢰 목록용
    private Long requestId;
    private String title;
    private String status; // "대기중", "진행중", "완료" 등
    private LocalDateTime createdAt;

    public MyRequestResponseDto(Request request) {
        this.requestId = request.getId();
        this.title = request.getTitle();
        this.status = request.getStatus();
        this.createdAt = request.getCreatedAt();
    }
}