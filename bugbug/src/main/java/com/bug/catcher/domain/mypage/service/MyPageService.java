package com.bug.catcher.domain.mypage.service;

import com.bug.catcher.domain.entity.*;
import com.bug.catcher.domain.hunter.dto.HunterProfileResponseDto;
import com.bug.catcher.domain.hunter.repository.*;
import com.bug.catcher.domain.hunter.service.HunterService;
import com.bug.catcher.domain.mypage.dto.*;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.domain.review.dto.ReviewResponseDto;
import com.bug.catcher.domain.review.repository.ReviewRepository;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class MyPageService {
    // 유저 정보를 DB에서 다시 확실하게 읽어오기 위해 UserRepository 주입
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final SavedHunterRepository savedHunterRepository;
    private final HunterRepository hunterRepository;
    private final ReviewRepository reviewRepository;
    private final HunterApplicationRepository hunterApplicationRepository;
    private final ApplicationRepository applicationRepository;
    private final SavedRequestRepository savedRequestRepository;
    private final HunterService hunterService;

    @Transactional(readOnly = true)
    public MyInfoResponseDto getMyInfo(Long userId) {
        //  DB에서 가장 최신의 유저 상태 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        //  마이페이지용 DTO로 변환하여 반환
        return new MyInfoResponseDto(user);
    }

    @Transactional
    public MyInfoResponseDto updateMyInfo(Long userId, MyInfoUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        String nickname = normalizeRequired(requestDto.getNickname(), "닉네임");
        String phoneNumber = normalizeOptional(requestDto.getPhoneNumber());
        String address = normalizeOptional(requestDto.getAddress());

        if (!user.getNickname().equals(nickname) && userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        user.updateProfile(nickname, phoneNumber, address);
        return new MyInfoResponseDto(user);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    //이슈2
    // 나의 의뢰 목록 조회 (페이징 적용)
    @Transactional(readOnly = true)
    public Page<MyRequestResponseDto> getMyRequests(Long userId, Pageable pageable) {
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(request -> new MyRequestResponseDto(
                        request,
                        reviewRepository.findByRequestId(request.getId()).orElse(null)
                ));
    }

    // 찜한 헌터 목록 보기
    @Transactional(readOnly = true)
    public Page<MySavedHunterResponseDto> getMySavedHunters(Long userId, Pageable pageable) {
        return savedHunterRepository.findByUserId(userId, pageable)
                .map(MySavedHunterResponseDto::new);
    }

    // 완료된 의뢰 리뷰 쓰기
    @Transactional
    public void createReview(Long userId, ReviewCreateRequestDto requestDto) {
        Request request = requestRepository.findById(requestDto.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 의뢰입니다."));

        validateReviewRating(requestDto.getRating());

        // 의뢰 작성자 본인만 리뷰를 작성할 수 있다.
        if (!request.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 의뢰에만 리뷰를 작성할 수 있습니다.");
        }

        // 리뷰는 완료 처리된 의뢰에만 작성할 수 있다.
        if (!"완료".equals(request.getStatus())) {
            throw new IllegalArgumentException("완료된 의뢰에만 리뷰를 작성할 수 있습니다.");
        }

        // 완료 헌터가 저장되어 있어야 리뷰 대상 헌터를 확정할 수 있다.
        Hunter completedHunter = request.getCompletedHunter();
        if (completedHunter == null) {
            throw new IllegalArgumentException("완료한 헌터 정보가 없어 리뷰를 작성할 수 없습니다.");
        }

        // 클라이언트가 보낸 헌터 ID가 완료 헌터와 다르면 다른 헌터에게 리뷰가 달리는 것을 막는다.
        if (requestDto.getHunterId() != null && !completedHunter.getId().equals(requestDto.getHunterId())) {
            throw new IllegalArgumentException("해당 의뢰를 완료한 헌터에게만 리뷰를 작성할 수 있습니다.");
        }

        // 하나의 의뢰에는 하나의 리뷰만 허용한다.
        if (reviewRepository.existsByRequestId(request.getId())) {
            throw new IllegalArgumentException("이미 리뷰가 작성된 의뢰입니다.");
        }

        Review review = Review.builder()
                .request(request)
                .hunter(completedHunter)
                .rating(requestDto.getRating())
                .reviewContent(requestDto.getReviewContent())
                .build();

        reviewRepository.save(review);
        //리뷰가 생성되었으니 해당 헌터의 등급 갱신
        hunterService.updateHunterLevel(completedHunter.getId());
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

        validateReviewRating(requestDto.getRating());

        // 엔티티 변경 감지를 사용해 수정일(updatedAt)까지 함께 갱신한다.
        review.update(requestDto.getRating(), requestDto.getReviewContent());
        // 평점이 바뀌었으니 헌터의 등급 갱신
        hunterService.updateHunterLevel(review.getHunter().getId());
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));
        // 삭제 권한 확인
        if (!review.getRequest().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }
        // [핵심] 삭제하기 전에 어떤 헌터의 리뷰였는지 ID를 미리 백업해 둡니다.
        Long targetHunterId = review.getHunter().getId();

        // 리뷰 삭제 실행
        reviewRepository.delete(review);

        //  백업해둔 헌터 ID를 이용해 등급 재산정 (리뷰가 지워졌으니 강등될 수도 있음!)
        hunterService.updateHunterLevel(targetHunterId);
    }
    //유저가 쓴 리뷰 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getMyReviews(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 헌터 마이페이지에서는 내가 작성한 리뷰가 아니라 내가 받은 리뷰를 보여준다.
        if ("HUNTER".equals(user.getRole())) {
            return reviewRepository.findByHunterUserId(userId, pageable)
                    .map(ReviewResponseDto::new);
        }

        return reviewRepository.findByUserId(userId, pageable)
                .map(ReviewResponseDto::new);
    }

    @Transactional(readOnly = true)
    public HunterFormResponseDto getHunterForm(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return new HunterFormResponseDto(user);
    }
    @Transactional
    public void applyForHunter(Long userId, HunterApplyRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 이미 신청한 내역이 있는지(대기 중인지) 검증하는 로직
         if (hunterApplicationRepository.existsByUserAndStatus(user, ApplicationStatus.PENDING)) {
             throw new IllegalArgumentException("이미 심사 대기 중인 신청서가 있습니다.");
         }

        HunterApplication application = HunterApplication.builder()
                .user(user)
                .name(user.getNickname()) // 팝업에서 따로 실명을 받는다면 requestDto에서 가져오기
                .pledgeAgreed(requestDto.getPledgeAgreed())
                .status(ApplicationStatus.PENDING) // 처음엔 무조건 대기 상태
                .createdAt(LocalDateTime.now())
                .build();

        hunterApplicationRepository.save(application);
    }
    // 1. 수행(수락)한 의뢰 목록 보기 (페이징 적용)
    @Transactional(readOnly = true)
    public Page<HunterTaskResponseDto> getHunterTasks(Long userId, Pageable pageable) {
        validateHunterProfileExists(userId);

        return applicationRepository.findByHunterUserId(userId, pageable)
                .map(HunterTaskResponseDto::new); // Page 객체의 map() 활용
    }

    // 2. 찜한 의뢰(게시물) 목록 보기 (페이징 적용)
    @Transactional(readOnly = true)
    public Page<HunterSavedRequestDto> getHunterSavedRequests(Long userId, Pageable pageable) {
        validateHunterProfileExists(userId);

        return savedRequestRepository.findByHunterUserId(userId, pageable)
                .map(HunterSavedRequestDto::new);
    }

    // 3. 헌터 등록 해제 (Role 강등)
    @Transactional
    public void resignHunter(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        // 유저의 권한을 다시 USER로 강등
        user.updateRole("USER");
    }
    @Transactional
    public HunterProfileResponseDto getMyHunterProfile(Long userId) {
        // 1. 유저 ID로 최신 헌터 엔티티 찾기
        Hunter hunter = hunterRepository.findTopByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("헌터 등록 정보가 없습니다."));

        long applicationCount = applicationRepository.countByHunterUserId(userId);
        float averageRating = reviewRepository.getAverageRatingByHunterUserId(userId);
        String grade = hunterService.calculateGrade(applicationCount, averageRating);

        hunter.updateGrade(grade);
        hunter.syncCompletionCount(applicationCount);

        return new HunterProfileResponseDto(hunter, applicationCount, averageRating);
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getMyHunterReviewSummary(Long userId) {
        validateHunterProfileExists(userId);

        Map<Integer, Long> ratingCounts = new LinkedHashMap<>();
        for (int score = 5; score >= 1; score--) {
            ratingCounts.put(score, 0L);
        }

        reviewRepository.findRatingsByHunterUserId(userId).stream()
                .filter(rating -> rating != null && rating > 0)
                .map(rating -> Math.max(1, Math.min(5, Math.round(rating))))
                .forEach(score -> ratingCounts.compute(score, (key, count) -> count + 1));

        return ratingCounts;
    }

    private void validateHunterProfileExists(Long userId) {
        hunterRepository.findTopByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("헌터 등록 정보가 없습니다."));
    }

    private void validateReviewRating(Float rating) {
        // 별점은 1점 단위이며 최대 5점까지만 허용한다.
        if (rating == null || rating < 1.0f || rating > 5.0f) {
            throw new IllegalArgumentException("별점은 1점 이상 5점 이하로 입력해 주세요.");
        }

        if (Math.abs(rating - Math.round(rating)) > 0.0001f) {
            throw new IllegalArgumentException("별점은 1점 단위로만 입력할 수 있습니다.");
        }
    }
}
