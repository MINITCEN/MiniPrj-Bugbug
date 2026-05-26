package com.bug.catcher.domain.hunter.dto;
import com.bug.catcher.domain.entity.Hunter;
import lombok.Getter;

@Getter
public class HunterProfileResponseDto {
    private Long hunterId;
    private String name;
    private String grade;
    private long completionCount;
    private float averageRating;
    private String gradeStory; // 등급별 재미있는 스토리 문구
    private long responseCount;

    public HunterProfileResponseDto(Hunter hunter, long completionCount, float averageRating) {
        this.hunterId = hunter.getId();
        this.name = hunter.getName();
        this.grade = hunter.getGrade();
        this.completionCount = completionCount;
        this.averageRating = averageRating;
        this.gradeStory = getGradeStory(hunter.getGrade());
        this.responseCount = completionCount;
    }

    private String getGradeStory(String grade) {
        return switch (grade) {
            case "루키" -> "버그버그에 첫발을 내디딘 신참 헌터입니다.";
            case "브론즈" -> "기본 장비와 본사 도구를 익힌 안정적인 헌터입니다.";
            case "실버" -> "상황을 빠르게 판단하고 현장을 정리하는 숙련된 요원입니다.";
            case "골드" -> "완벽한 차단과 복합 장비로 현장을 관리하는 전문가입니다.";
            case "레전드" -> "수많은 현장을 평정한 최고의 해결사입니다.";
            default -> "정보를 불러올 수 없습니다.";
        };
    }
}
