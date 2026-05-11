package com.bug.catcher.domain.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewUpdateRequestDto {
    private Float rating;
    private String reviewContent;
}