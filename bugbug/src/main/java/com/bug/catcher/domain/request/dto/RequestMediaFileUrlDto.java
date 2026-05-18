package com.bug.catcher.domain.request.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestMediaFileUrlDto {
    private Long requestId;

    // 삭제할 이미지 URL 목록
    private List<String> imageUrls = new ArrayList<>();
    // 삭제할 동영상 URL
    private String videoUrl;
}
