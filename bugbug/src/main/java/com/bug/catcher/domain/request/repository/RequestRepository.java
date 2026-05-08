package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Request r set r.videoUrl = :videoUrl where r.id = :requestId")
    int updateVideoUrl(Long requestId, String videoUrl);
}
