package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.RequestImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestImageRepository extends JpaRepository<RequestImage, Long> {

    List<RequestImage> findByRequestId(Long requestId);
}