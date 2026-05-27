package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // 헌터 마이페이지에서 헌터가 수행 중인 의뢰 목록을 페이징 조회한다.
    Page<Application> findByHunterId(Long hunterId, Pageable pageable);
    Page<Application> findByHunterUserId(Long userId, Pageable pageable);

    // 같은 헌터가 같은 의뢰에 중복 지원하지 못하도록 확인한다.
    boolean existsByRequestIdAndHunterId(Long requestId, Long hunterId);


    List<Application> findByRequestId(Long requestId);
    long countByHunterId(Long hunterId);
    long countByHunterUserId(Long userId);
    long countByHunterIdAndRequest_Status(Long hunterId, String status);
}
