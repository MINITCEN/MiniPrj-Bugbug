package com.bug.catcher.domain.mypage.service;

import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.Review;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.hunter.repository.SavedHunterRepository;
import com.bug.catcher.domain.mypage.dto.MyInfoResponseDto;
import com.bug.catcher.domain.mypage.dto.MyRequestResponseDto;
import com.bug.catcher.domain.mypage.dto.MySavedHunterResponseDto;
import com.bug.catcher.domain.mypage.dto.ReviewCreateRequestDto;
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
        // 1. DB에서 가장 최신의 유저 상태 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 2. 마이페이지용 DTO로 변환하여 반환
        return new MyInfoResponseDto(user);
    }
    //이슈2
    // 1. 나의 의뢰 목록 보기
    @Transactional(readOnly = true)
    public List<MyRequestResponseDto> getMyRequests(Long userId) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(MyRequestResponseDto::new)
                .toList();
    }

    // 2. 찜한 헌터 목록 보기
    @Transactional(readOnly = true)
    public List<MySavedHunterResponseDto> getMySavedHunters(Long userId) {
        return savedHunterRepository.findByUserId(userId)
                .stream()
                .map(MySavedHunterResponseDto::new)
                .toList();
    }

    // 3. 완료된 의뢰 리뷰 쓰기
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
}
