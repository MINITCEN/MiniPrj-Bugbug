package com.bug.catcher.domain.admin.dto;

import com.bug.catcher.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserResponseDto {
    private Long id;
    private String nickname;
    private String email;
    private String phoneNumber;
    private String address;
    private String role;
    private String accountStatus; // 계정 정지 여부 확인용

    // User 엔티티를 DTO로 변환하는 편의 메서드
    public static AdminUserResponseDto from(User user) {
        return AdminUserResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus() != null ? user.getAccountStatus().name() : "ACTIVE")
                .build();
    }
}
