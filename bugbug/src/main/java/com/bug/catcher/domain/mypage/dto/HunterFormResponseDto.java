package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.User;
import lombok.Getter;

@Getter
public class HunterFormResponseDto {// 팝업 시 기존 정보 내려줌

    private String email;       // 이메일 (수정 불가용)
    private String nickname;    // 기존 닉네임
    private String phoneNumber; // 기존 전화번호
    private String address;     // 기존 주소

    public HunterFormResponseDto(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
    }
}
