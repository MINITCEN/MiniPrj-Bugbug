package com.bug.catcher.domain.request.repository;

import com.bug.catcher.domain.entity.RequestImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RequestImageRepository extends JpaRepository<RequestImage, Long> {
    List<RequestImage> findByRequest_Id(Long requestId);
    @Modifying
    @Query("delete from RequestImage i where i.request.id = :requestId and i.imageUrl in :imageUrls")
    void deleteByRequestIdAndImageUrls(Long requestId, List<String> imageUrls);
}