package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.Hunter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HunterRepository extends JpaRepository<Hunter, Long> {
}
