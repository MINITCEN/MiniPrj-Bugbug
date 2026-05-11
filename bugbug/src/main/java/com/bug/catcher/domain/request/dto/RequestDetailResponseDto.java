package com.bug.catcher.domain.request.dto;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.RequestImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RequestDetailResponseDto {
    private long requestId;
    private String title;
    private String content;
    private String status;
    private String approxLocation;
    private String exactLocation;
    private LocalDateTime occurrenceTime;
    private Integer viewCount;
    private String videoUrl;
    private List<String> imageUrls;

    public static RequestDetailResponseDto responseDto(Request request){
        return RequestDetailResponseDto.builder()
                .requestId(request.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .approxLocation(request.getApproxLocation())
                .exactLocation(request.getExactLocation())
                .occurrenceTime(request.getOccurrenceTime())
                .viewCount(request.getViewCount())
                .videoUrl(request.getVideoUrl())
                .imageUrls(request.getRequestImages().stream().map(RequestImage :: getImageUrl).toList())
                .build();
    }
}
