package com.bug.catcher.domain.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyInfoUpdateRequestDto {
    private String nickname;
    private String phoneNumber;
    private String address;
}
