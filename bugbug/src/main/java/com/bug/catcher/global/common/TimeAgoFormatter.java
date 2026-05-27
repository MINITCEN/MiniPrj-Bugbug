package com.bug.catcher.global.common;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * 전역 공통 시간 포맷터 유틸리티 클래스입니다.
 * 절대 시간(LocalDateTime)을 기준으로 현재 시간과의 오차를 계산하여,
 * 사용자가 직관적으로 읽을 수 있는 상대적 경과 시간("N분 전", "N시간 전" 등)으로 변환합니다.
 */
public class TimeAgoFormatter {

    // 정적 유틸리티 클래스이므로 인스턴스화 방지
    private TimeAgoFormatter() {}

    /**
     * 입력된 일시와 현재 시간의 차이를 계산하여 알맞은 상대 시간 포맷으로 변환합니다.
     *
     * @param createdAt 비교 대상이 되는 과거 일시 (예: 게시글 작성 시간)
     * @return 계산된 상대 시간 문자열 (예: "방금 전", "5분 전", "3시간 전", "2일 전" 등)
     */
    public static String format(LocalDateTime createdAt) {
        // 방어적 예외 처리: 일시 데이터가 누락(null)되어 들어오는 경우 빈 문자열 반환
        if (createdAt == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 과거 등록 시점과 현재 시점 사이의 절대 시간 차이 계산
        long seconds = Duration.between(createdAt, now).getSeconds();
        
        // 1. 60초(1분) 미만 경과 시
        if (seconds < 60) {
            return "방금 전";
        }
        
        // 2. 60분(1시간) 미만 경과 시 (분 단위 표기)
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "분 전";
        }
        
        // 3. 24시간(1일) 미만 경과 시 (시간 단위 표기)
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }
        
        // 4. 30일(1달) 미만 경과 시 (일 단위 표기)
        long days = hours / 24;
        if (days < 30) {
            return days + "일 전";
        }
        
        // 5. 12달(1년) 미만 경과 시 (달 단위 표기)
        long months = days / 30;
        if (months < 12) {
            return months + "개월 전";
        }
        
        // 6. 1년 이상 경과 시 (상대 시간이 직관적이지 않으므로 년-월-일 절대 날짜 표기)
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
