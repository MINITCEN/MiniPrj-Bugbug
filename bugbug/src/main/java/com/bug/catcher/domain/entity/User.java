package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    private String phoneNumber;
    private String address;

    @Column(nullable = false, length = 20)
    private String role;

    // 개인정보 수집 동의 여부 추가
    @Column(nullable = false)
    private Boolean isPrivacyAgreed;

    // 계정 상태 관리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    // 정지 해제 일시
    private java.time.LocalDateTime banEndDate;

    // 계정 정지 메서드
    public void suspend(java.time.LocalDateTime banEndDate) {
        this.accountStatus = AccountStatus.SUSPENDED;
        this.banEndDate = banEndDate;
    }

    // 계정 복구(활성화) 메서드
    public void activate() {
        this.accountStatus = AccountStatus.ACTIVE;
        this.banEndDate = null;
    }

    // 권한 변경 메서드 (일반 유저 -> 헌터)
    public void updateRole(String role) {
        this.role = role;
    }

    public void updateProfile(String nickname, String phoneNumber, String address) {
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
