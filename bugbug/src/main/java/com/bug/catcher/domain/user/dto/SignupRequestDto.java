package com.bug.catcher.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {
    private String email;
    private String password;
    private String nickname;
    private String phoneNumber;
    private String address;

    // 주의: role(권한) 필드는 넣지 않습니다!
    // 클라이언트(프론트엔드)에서 임의로 "ADMIN" 이라고 보내서 가입하는 해킹을 막기 위함입니다.
}