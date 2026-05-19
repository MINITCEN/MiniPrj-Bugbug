package com.bug.catcher.domain.entity;

public enum AccountStatus {
    ACTIVE,      // 정상 활동 중
    SUSPENDED,   // 정지됨 (banEndDate 필요)
    WITHDRAWN    // 탈퇴함
}
