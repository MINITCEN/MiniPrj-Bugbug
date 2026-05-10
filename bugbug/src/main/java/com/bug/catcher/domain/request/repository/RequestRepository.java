package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface
RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUserIdOrderByCreatedAtDesc(Long userId);

}
