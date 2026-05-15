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

    // 화면에서는 location, detailLocation으로 받고
    // 서비스에서 Request.approxLocation, Request.exactLocation으로 매핑
    private String location;
    private String detailLocation;
    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime occurrenceTime;
    private Double latitude;
    private Double longitude;
    private String occurrencePlace;
    private String additionalDescription;

    // 이미지 여러 장 업로드
    private List<MultipartFile> imageFiles = new ArrayList<>();

    // 동영상 한 개 업로드
    private MultipartFile videoFile;

    // 수정 화면에서 삭제하지 않고 유지한 기존 이미지 URL 목록
    private List<String> imageUrls = new ArrayList<>();

    // 수정 화면에서 삭제하지 않고 유지한 기존 비디오 URL
    private String videoUrl;
}