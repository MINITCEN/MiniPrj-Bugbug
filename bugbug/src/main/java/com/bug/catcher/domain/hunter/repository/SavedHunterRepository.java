package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.SavedHunter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedHunterRepository extends JpaRepository<SavedHunter, Long> {
    // 유저 ID로 찜한 헌터 목록을 찾는 메서드
    List<SavedHunter> findByUserId(Long userId);
}