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
    private Long requestId;
    private Long userId;
    private String nickname;
    private String userRole;
    private String title;
    private String description;
    private String content;
    private String status;
    private String approxLocation;
    private String exactLocation;
    private LocalDateTime occurrenceTime;
    private Integer viewCount;
    private String videoUrl;
    private List<String> imageUrls;
    private Long completedHunterId;
    private String completedHunterName;

    public static RequestDetailResponseDto responseDto(Request request){
        return RequestDetailResponseDto.builder()
                .requestId(request.getId())
                .userId(request.getUser().getId())
                .userRole(request.getUser().getRole())
                .title(request.getTitle())
                .content(request.getContent())
                .status(request.getStatus())
                .nickname(request.getUser().getNickname())
                .description(request.getDescription())
                .approxLocation(request.getApproxLocation())
                .exactLocation(request.getExactLocation())
                .occurrenceTime(request.getOccurrenceTime())
                .viewCount(request.getViewCount())
                .videoUrl(request.getVideoUrl())
                .imageUrls(request.getRequestImages().stream().map(RequestImage :: getImageUrl).toList())
                .completedHunterId(request.getCompletedHunter() != null ? request.getCompletedHunter().getId() : null)
                .completedHunterName(request.getCompletedHunter() != null ? request.getCompletedHunter().getName() : null)
                .build();
    }
}
