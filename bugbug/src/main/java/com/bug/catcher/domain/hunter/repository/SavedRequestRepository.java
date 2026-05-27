package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.SavedRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedRequestRepository extends JpaRepository<SavedRequest, Long> {
    // List -> Page로 변경, Pageable 추가
    Page<SavedRequest> findByHunterId(Long hunterId, Pageable pageable);
    Page<SavedRequest> findByHunterUserId(Long userId, Pageable pageable);

    boolean existsByHunterIdAndRequestId(Long hunterId, Long requestId);
    void deleteByHunterIdAndRequestId(Long hunterId, Long requestId);
}
