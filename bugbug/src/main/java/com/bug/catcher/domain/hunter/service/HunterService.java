package com.bug.catcher.domain.hunter.service;

import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HunterService {//등급 산정 엔진과 외부 공개 프로필 조회용
    private final HunterRepository hunterRepository;
    private final ReviewRepository reviewRepository;

    // 헌터 레벨 산정 및 업데이트 엔진
    @Transactional
    public void updateHunterLevel(Long hunterId) {
        Hunter hunter = hunterRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("헌터를 찾을 수 없습니다."));

        long completionCount = reviewRepository.countByHunterId(hunterId);
        float averageRating = reviewRepository.getAverageRatingByHunterId(hunterId);

        String newGrade = "슬리퍼 전사"; // Lv 1 (기본)

        // 높은 레벨의 조건부터 역순으로 검사하여 매칭
        if (completionCount >= 100 && averageRating >= 4.8f) {
            newGrade = "해충의 종말"; // Lv 5
        } else if (completionCount >= 30 && averageRating >= 4.5f) {
            newGrade = "버그 이레이저"; // Lv 4
        } else if (completionCount >= 15 && averageRating >= 4.0f) {
            newGrade = "일렉트로닉 가디언"; // Lv 3
        } else if (completionCount >= 5) {
            newGrade = "스프레이 스나이퍼"; // Lv 2
        }

        // 산정된 등급으로 즉시 업데이트
        hunter.updateGrade(newGrade);
    }
    @Transactional(readOnly = true)
    public HunterProfileResponseDto getHunterProfile(Long hunterId) {
        Hunter hunter = hunterRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 헌터입니다."));

        long completionCount = reviewRepository.countByHunterId(hunterId);
        float averageRating = reviewRepository.getAverageRatingByHunterId(hunterId);

        return new HunterProfileResponseDto(hunter, completionCount, averageRating);
    }
}
