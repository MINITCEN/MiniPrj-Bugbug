package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, Long> {

}
