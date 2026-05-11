package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByHunterId(Long hunterId);
}