package com.bug.catcher.domain.review.dto;

import com.bug.catcher.domain.entity.Review;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponseDto {
    private Long reviewId;
    private String hunterName;    // 유저가 볼 때: 어떤 헌터에게 썼는지
    private String userName;      // 헌터 프로필에서 볼 때: 누가 썼는지
    private Float rating;
    private String content;


    public ReviewResponseDto(Review review) {
        this.reviewId = review.getId();
        this.hunterName = review.getHunter().getName();
        this.userName = review.getRequest().getUser().getNickname();
        this.rating = review.getRating();
        this.content = review.getReviewContent();

    }
}