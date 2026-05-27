package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.Hunter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HunterRepository extends JpaRepository<Hunter, Long> {
    Optional<Hunter> findByUserId(Long userId);
    Optional<Hunter> findTopByUserIdOrderByIdDesc(Long userId);
}
