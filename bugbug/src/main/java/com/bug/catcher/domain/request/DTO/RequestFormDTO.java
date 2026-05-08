package com.bug.catcher.domain.request.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestFormDTO {
    // Request 엔티티에 직접 매핑 가능한 값
    private String title;

    private String content;

    private String location;        // Request.approxLocation으로 저장
    private String detailLocation;  // Request.exactLocation으로 저장

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime preferredTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime occurrenceTime;

    private Double latitude;
    private Double longitude;

    private String occurrencePlace;
    private String additionalDescription;

    // RequestImage 엔티티로 저장할 값
    private List<MultipartFile> imageFiles = new ArrayList<>();
}
