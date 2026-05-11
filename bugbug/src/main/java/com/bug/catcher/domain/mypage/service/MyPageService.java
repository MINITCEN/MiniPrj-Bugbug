package com.bug.catcher.domain.mypage.service;

import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.Review;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.hunter.repository.SavedHunterRepository;
import com.bug.catcher.domain.mypage.dto.*;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.domain.review.repository.ReviewRepository;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {
    // 유저 정보를 DB에서 다시 확실하게 읽어오기 위해 UserRepository 주입
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final SavedHunterRepository savedHunterRepository;
    private final HunterRepository hunterRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public MyInfoResponseDto getMyInfo(Long userId) {
        //  DB에서 가장 최신의 유저 상태 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        //  마이페이지용 DTO로 변환하여 반환
        return new MyInfoResponseDto(user);
    }
    //이슈2
    //  나의 의뢰 목록 보기
    @Transactional(readOnly = true)
    public List<MyRequestResponseDto> getMyRequests(Long userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MyRequestResponseDto::new)
                .toList();
    }

    // 찜한 헌터 목록 보기
    @Transactional(readOnly = true)
    public List<MySavedHunterResponseDto> getMySavedHunters(Long userId) {
        return savedHunterRepository.findByUserId(userId)
                .stream()
                .map(MySavedHunterResponseDto::new)
                .toList();
    }

    // 완료된 의뢰 리뷰 쓰기
    @Transactional
    public void createReview(Long userId, ReviewCreateRequestDto requestDto) {
        Request request = requestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 의뢰입니다."));

        Hunter hunter = hunterRepository.findById(requestDto.getHunterId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 헌터입니다."));

        // 유저 본인의 의뢰가 맞는지, 상태가 '완료'인지 검증하는 로직 추가 가능
        if (!request.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 의뢰에만 리뷰를 작성할 수 있습니다.");
        }

        Review review = Review.builder()
                .request(request)
                .hunter(hunter)
                .rating(requestDto.getRating())
                .reviewContent(requestDto.getReviewContent())
                .build();

        reviewRepository.save(review);
    }
    // 리뷰 수정
    @Transactional
    public void updateReview(Long userId, Long reviewId, ReviewUpdateRequestDto requestDto) {
        // 1. 리뷰와 작성자 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if (!review.getRequest().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        //  엔티티 메서드 대신 레포지토리의 직접 업데이트 쿼리 실행
        reviewRepository.updateReviewDirectly(
                reviewId,
                requestDto.getRating(),
                requestDto.getReviewContent()
        );
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        //  삭제 권한 확인
        if (!review.getRequest().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }
}
