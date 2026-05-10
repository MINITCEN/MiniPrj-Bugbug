package com.bug.catcher.domain.review.repository;

import com.bug.catcher.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}