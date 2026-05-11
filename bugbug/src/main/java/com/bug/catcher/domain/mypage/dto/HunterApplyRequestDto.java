package com.bug.catcher.domain.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HunterApplyRequestDto { // 헌터등록시 신청서를 제출할 때 받는 용도
    private Boolean pledgeAgreed;
}
