package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "hunters")
public class Hunter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hunter_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    //나이, 성별 삭제
    private Boolean pledgeAgreed;
    private String grade;
    private Integer requestCount;
    private Integer responseCount;
    //  레벨업 시 등급을 변경하는 메서드
    public void updateGrade(String newGrade) {
        this.grade = newGrade;
    }

    public void updateProfile(String name, Boolean pledgeAgreed) {
        this.name = name;
        this.pledgeAgreed = pledgeAgreed;
    }

    public void syncCompletionCount(long completionCount) {
        int count = Math.toIntExact(completionCount);
        this.requestCount = count;
        this.responseCount = count;
    }
}
