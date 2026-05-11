package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.HunterApplication;
import com.bug.catcher.domain.entity.ApplicationStatus;
import com.bug.catcher.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HunterApplicationRepository extends JpaRepository<HunterApplication, Long> {
    boolean existsByUserAndStatus(User user, ApplicationStatus status);
}