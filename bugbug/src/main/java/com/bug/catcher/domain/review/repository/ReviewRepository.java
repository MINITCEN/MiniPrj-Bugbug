package com.bug.catcher.domain.review.repository;

import com.bug.catcher.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 헌터의 완료 건수는 리뷰가 작성된 건수 기준으로 계산한다.
    long countByHunterId(Long hunterId);

    // 하나의 의뢰에는 하나의 리뷰만 작성할 수 있도록 중복 작성 여부를 확인한다.
    boolean existsByRequestId(Long requestId);

    // 나의 의뢰 목록에서 리뷰 작성 여부와 기존 리뷰 ID를 함께 내려주기 위해 사용한다.
    Optional<Review> findByRequestId(Long requestId);

    // 헌터의 평균 별점은 리뷰 테이블에서 실시간 집계한다.
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.hunter.id = :hunterId")
    Float getAverageRatingByHunterId(@Param("hunterId") Long hunterId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.hunter.user.id = :userId")
    Float getAverageRatingByHunterUserId(@Param("userId") Long userId);

    // 의뢰인이 본인이 작성한 리뷰를 조회할 때 사용한다.
    @Query("SELECT r FROM Review r WHERE r.request.user.id = :userId")
    Page<Review> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 헌터 마이페이지와 헌터 공개 프로필에서 받은 리뷰를 조회할 때 사용한다.
    Page<Review> findByHunterId(Long hunterId, Pageable pageable);

    // 헌터 로그인 계정 기준으로 본인이 받은 리뷰를 조회한다.
    Page<Review> findByHunterUserId(Long userId, Pageable pageable);

    // 헌터 로그인 계정 기준으로 본인이 받은 리뷰 평점만 조회한다.
    @Query("SELECT r.rating FROM Review r WHERE r.hunter.user.id = :userId")
    List<Float> findRatingsByHunterUserId(@Param("userId") Long userId);
}
