package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyRequestResponseDto {
    private Long requestId;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private Long completedHunterId;
    private String completedHunterName;
    private Long reviewId;
    private Float reviewRating;
    private String reviewContent;
    private LocalDateTime reviewCreatedAt;
    private boolean reviewed;
    private boolean reviewable;

    public MyRequestResponseDto(Request request, Review review) {
        this.requestId = request.getId();
        this.title = request.getTitle();
        this.status = request.getStatus();
        this.createdAt = request.getCreatedAt();
        this.completedHunterId = request.getCompletedHunter() != null ? request.getCompletedHunter().getId() : null;
        this.completedHunterName = request.getCompletedHunter() != null ? request.getCompletedHunter().getName() : null;
        this.reviewId = review != null ? review.getId() : null;
        this.reviewRating = review != null ? review.getRating() : null;
        this.reviewContent = review != null ? review.getReviewContent() : null;
        this.reviewCreatedAt = review != null ? review.getCreatedAt() : null;
        this.reviewed = review != null;
        // 의뢰인이 완료된 의뢰의 완료 헌터에게 아직 리뷰를 쓰지 않은 경우에만 작성 버튼을 노출한다.
        this.reviewable = "완료".equals(request.getStatus())
                && request.getCompletedHunter() != null
                && review == null;
    }
}
