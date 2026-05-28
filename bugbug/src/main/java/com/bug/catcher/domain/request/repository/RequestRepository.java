package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Modifying
    @Query("update Request r set r.viewCount = r.viewCount + 1 where r.id = :requestId")
    int increaseViewCount(Long requestId);

    boolean existsByIdAndUser_Id(Long requestId, Long userId);

    //상태별 페이지 조회
    Page<Request> findByStatus(String status, Pageable pageable);

    @Query("select r.videoUrl from Request r where r.id = :requestId and r.user.id = :loginUserId")
    String findVideoUrlByRequestIdAndUserId(Long requestId, Long loginUserId);

    @Modifying
    @Query("update Request r set r.videoUrl = :videoUrl where r.id = :requestId and r.user.id = :loginUserId")
    int updateVideoUrl(Long requestId, Long loginUserId, String videoUrl);

    Page<Request> findByUserIdOrderByCreatedAtDesc (Long userId, Pageable pageable);
}
