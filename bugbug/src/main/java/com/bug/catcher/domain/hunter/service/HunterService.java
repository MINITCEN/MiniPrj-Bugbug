package com.bug.catcher.domain.hunter.service;

import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.SavedHunter;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.hunter.repository.ApplicationRepository;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.hunter.repository.SavedHunterRepository;
import com.bug.catcher.domain.review.dto.ReviewResponseDto;
import com.bug.catcher.domain.review.repository.ReviewRepository;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bug.catcher.domain.hunter.dto.HunterListResponseDto;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HunterService {//등급 산정 엔진과 외부 공개 프로필 조회용
    private final HunterRepository hunterRepository;
    private final ReviewRepository reviewRepository;
    private final SavedHunterRepository savedHunterRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    // 헌터 레벨 산정 및 업데이트 엔진
    @Transactional
    public void updateHunterLevel(Long hunterId) {
        Hunter hunter = hunterRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("헌터를 찾을 수 없습니다."));

        long applicationCount = applicationRepository.countByHunterId(hunterId);
        float averageRating = reviewRepository.getAverageRatingByHunterId(hunterId);

        String newGrade = calculateGrade(applicationCount, averageRating);

        // 산정된 등급으로 즉시 업데이트
        hunter.updateGrade(newGrade);
        hunter.syncCompletionCount(applicationCount);
    }

    @Transactional
    public HunterProfileResponseDto getHunterProfile(Long hunterId) {
        Hunter hunter = hunterRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 헌터입니다."));

        long applicationCount = applicationRepository.countByHunterId(hunterId);
        float averageRating = reviewRepository.getAverageRatingByHunterId(hunterId);
        String grade = calculateGrade(applicationCount, averageRating);

        hunter.updateGrade(grade);
        hunter.syncCompletionCount(applicationCount);

        return new HunterProfileResponseDto(hunter, applicationCount, averageRating);
    }

    //헌터 찜하기 / 찜 해제
    @Transactional
    public String toggleSavedHunter(Long userId, Long hunterId) {
        // 이미 찜한 상태면 찜 해제
        if (savedHunterRepository.existsByUserIdAndHunterId(userId, hunterId)) {
            savedHunterRepository.deleteByUserIdAndHunterId(userId, hunterId);
            return "헌터 찜하기가 취소되었습니다.";
        }

        // 찜한 상태아니면 찜하기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        Hunter hunter = hunterRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 헌터입니다."));

        SavedHunter savedHunter = SavedHunter.builder()
                .user(user)
                .hunter(hunter)
                .build();

        savedHunterRepository.save(savedHunter);
        return "헌터를 찜했습니다!";
    }

    // 전체 헌터 목록 조회 (페이징 + 찜 여부 포함)
    @Transactional(readOnly = true)
    public Page<HunterListResponseDto> getHunterList(Long userId, Pageable pageable) {
        // 1. 전체 헌터 목록을 페이징해서 가져옴
        Page<Hunter> hunters = hunterRepository.findAll(pageable);

        // 2. 현재 로그인한 유저가 찜한 '헌터 ID 목록'만 한 번에 싹 가져와서 Set(바구니)에 담아둠 (성능 최적화)
        Set<Long> myBookmarkedHunterIds = userId == null
                ? Collections.emptySet()
                : savedHunterRepository.findByUserId(userId)
                        .stream()
                        .map(saved -> saved.getHunter().getId())
                        .collect(Collectors.toSet());

        // 3. 헌터 목록을 DTO로 변환하면서, 아까 담아둔 Set(바구니)에 이 헌터 ID가 있는지 확인하여 isBookmarked 설정
        return hunters.map(hunter -> new HunterListResponseDto(
                hunter,
                myBookmarkedHunterIds.contains(hunter.getId()),
                applicationRepository.countByHunterId(hunter.getId())
        ));
    }
    //헌터가 받은 리뷰 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getHunterReviews(Long hunterId, Pageable pageable) {
        return reviewRepository.findByHunterId(hunterId, pageable)
                .map(ReviewResponseDto::new);
    }

    public String calculateGrade(long applicationCount, float averageRating) {
        if (applicationCount >= 40 && averageRating >= 4.8f) {
            return "레전드";
        }
        if (applicationCount >= 20) {
            return "골드";
        }
        if (applicationCount >= 10) {
            return "실버";
        }
        if (applicationCount >= 3) {
            return "브론즈";
        }
        return "루키";
    }
}
