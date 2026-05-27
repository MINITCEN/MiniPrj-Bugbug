package com.bug.catcher.domain.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequestDto { // 리뷰 작성 입력
    private Long requestId; // 어떤 의뢰에 대한 리뷰인지
    private Long hunterId;  // 어떤 헌터에게 남기는 건지
    private Float rating;   // 별점 (예: 4)
    private String reviewContent; // 리뷰 내용
}
