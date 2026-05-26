package com.bug.catcher.domain.request.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestFormDto {
    // Request 엔티티에 저장될 기본 입력값
    private String title;
    private String content;

    // 서비스에서 Request.approxLocation, Request.exactLocation으로 매핑
    private String location;
    private String detailLocation;
    private String status;
    private Long completedHunterId;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime occurrenceTime;
    private Double latitude;
    private Double longitude;
    private String description;

    // 이미지 여러 장 업로드
    private List<MultipartFile> imageFiles = new ArrayList<>();

    // 동영상 한 개 업로드
    private MultipartFile videoFile;
}
