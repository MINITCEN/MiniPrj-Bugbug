package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // List -> Page로 변경, Pageable 추가
    Page<Application> findByHunterId(Long hunterId, Pageable pageable);
}