package com.bug.catcher.domain.review.repository;

import com.bug.catcher.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    //  엔티티를 건드리지 않고 직접 DB 업데이트 쿼리를 날림
    @Modifying
    @Query("UPDATE Review r SET r.rating = :rating, r.reviewContent = :content WHERE r.id = :reviewId")
    void updateReviewDirectly(
            @Param("reviewId") Long reviewId,
            @Param("rating") Float rating,
            @Param("content") String content
    );
}