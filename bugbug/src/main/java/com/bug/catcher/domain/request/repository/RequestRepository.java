package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.Request;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

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

    @Modifying
    @Query("delete " +
            "from Request r " +
            "where r.id = :requestId and r.user.id = :loginUserId")
    int delete(Long requestId, Long loginUserId);

    List<Request> findByUserIdOrderByCreatedAtDesc(Long userId);
}
