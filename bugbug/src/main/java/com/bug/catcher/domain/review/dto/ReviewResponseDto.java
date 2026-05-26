package com.bug.catcher.domain.review.dto;

import com.bug.catcher.domain.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {
    private Long reviewId;
    private Long requestId;
    private String requestTitle;
    private String hunterName;
    private String userName;
    private Float rating;
    private String content;
    private LocalDateTime createdAt;

    public ReviewResponseDto(Review review) {
        this.reviewId = review.getId();
        this.requestId = review.getRequest().getId();
        this.requestTitle = review.getRequest().getTitle();
        this.hunterName = review.getHunter().getName();
        this.userName = review.getRequest().getUser().getNickname();
        this.rating = review.getRating();
        this.content = review.getReviewContent();
        this.createdAt = review.getCreatedAt();
    }
}
