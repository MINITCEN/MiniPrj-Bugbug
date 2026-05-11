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

    public HunterProfileResponseDto(Hunter hunter, long completionCount, float averageRating) {
        this.hunterId = hunter.getId();
        this.name = hunter.getName();
        this.grade = hunter.getGrade();
        this.completionCount = completionCount;
        this.averageRating = averageRating;
        this.gradeStory = getGradeStory(hunter.getGrade());
    }

    private String getGradeStory(String grade) {
        return switch (grade) {
            case "슬리퍼 전사" -> "장비가 없으면 몸으로 때운다! 아직은 조준이 서툴지만 용기만큼은 일류입니다.";
            case "스프레이 스나이퍼" -> "치익- 소리만으로 벌레들을 공포에 떨게 하는 원거리 교전 숙련병입니다.";
            case "일렉트로닉 가디언" -> "내 구역에 자비란 없다. 단 한 마리의 탈출도 불허하는 전기 파리채의 달인입니다.";
            case "버그 이레이저" -> "제가 다녀간 곳엔 먼지조차 남지 않습니다. 존재 자체를 지워버리는 청소부입니다.";
            case "해충의 종말" -> "그가 나타나면 해당 지역 해충들이 스스로 이삿짐을 싼다는 전설의 최후 헌터입니다.";
            default -> "정보를 불러올 수 없습니다.";
        };
    }
}
