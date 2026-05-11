package com.bug.catcher.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardResponseDto {
    private String role;     // "USER" 또는 "HUNTER"
    private String nickname; // 유저 닉네임
}
