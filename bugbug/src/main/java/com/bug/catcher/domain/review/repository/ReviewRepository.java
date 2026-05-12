package com.bug.catcher.domain.review.repository;

import com.bug.catcher.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 엔티티를 건드리지 않고 직접 DB 업데이트 쿼리를 날림
    @Modifying
    @Query("UPDATE Review r SET r.rating = :rating, r.reviewContent = :content WHERE r.id = :reviewId")
    void updateReviewDirectly(
            @Param("reviewId") Long reviewId,
            @Param("rating") Float rating,
            @Param("content") String content
    );

    // 헌터의 총 완료(리뷰) 횟수 계산
    long countByHunterId(Long hunterId);

    // 헌터의 평균 평점 계산 (리뷰가 하나도 없으면 0.0 반환)
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.hunter.id = :hunterId")
    Float getAverageRatingByHunterId(@Param("hunterId") Long hunterId);

    // 유저가 작성한 리뷰 목록 조회 (작성자 ID 기준)
    @Query("SELECT r FROM Review r WHERE r.request.user.id = :userId")
    Page<Review> findByUserId(@Param("userId") Long userId, Pageable pageable);

    // 특정 헌터가 받은 리뷰 목록 조회 (헌터 ID 기준)
    Page<Review> findByHunterId(Long hunterId, Pageable pageable);
}