package com.bug.catcher.domain.hunter.repository;

import com.bug.catcher.domain.entity.SavedHunter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedHunterRepository extends JpaRepository<SavedHunter, Long> {
    // 유저 ID로 찜한 헌터 목록을 찾는 메서드
    List<SavedHunter> findByUserId(Long userId);
    Page<SavedHunter> findByUserId(Long userId, Pageable pageable);
    // 이미 찜한 헌터인지 확인
    boolean existsByUserIdAndHunterId(Long userId, Long hunterId);
    //  찜 해제
    void deleteByUserIdAndHunterId(Long userId, Long hunterId);
}
