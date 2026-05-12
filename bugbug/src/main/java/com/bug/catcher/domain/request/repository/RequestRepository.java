package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Modifying
    @Query("update Request r set r.viewCount = r.viewCount + 1 where r.id = :requestId")
    int increaseViewCount(Long requestId);

    @Modifying
    @Query("update Request r " +
            "set r.title = :title, r.content = :content, r.approxLocation = :location, r.exactLocation = :detailLocation, r.occurrenceTime = :occurrenceTime " +
            "where r.id = :requestId and r.user.id = :loginUserId")
    int update(
            Long requestId,
            Long loginUserId,
            String title,
            String content,
            String location,
            String detailLocation,
            LocalDateTime occurrenceTime
    );

    Optional<Request> findByIdAndUser_Id(Long requestId, Long userId);

    @Query("select r.videoUrl from Request r where r.id = :requestId and r.user.id = :loginUserId")
    String findVideoUrlByRequestIdAndUserId(Long requestId, Long loginUserId);

    @Modifying
    @Query("update Request r set r.videoUrl = :videoUrl where r.id = :requestId and r.user.id = :loginUserId")
    int updateVideoUrl(Long requestId, Long loginUserId, String videoUrl);

    List<Request> findByUserIdOrderByCreatedAtDesc(Long userId);
}