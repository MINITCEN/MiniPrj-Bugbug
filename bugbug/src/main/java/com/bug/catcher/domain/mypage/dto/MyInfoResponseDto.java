package com.bug.catcher.domain.mypage.dto;

import com.bug.catcher.domain.entity.User;
import lombok.Getter;

@Getter
public class MyInfoResponseDto {
    private String email;

    private String nickname;
    private String phoneNumber;
    private String address;
    private String role; // "USER" 또는 "HUNTER"

    // User 엔티티를 받아서 필요한 정보 담음.
    public MyInfoResponseDto(User user) {
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.phoneNumber = user.getPhoneNumber();
        this.address = user.getAddress();
        this.role = user.getRole();
    }
}