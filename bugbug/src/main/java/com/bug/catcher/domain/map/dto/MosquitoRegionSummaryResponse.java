package com.bug.catcher.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "선택 지역 상세 요약 응답")
public record MosquitoRegionSummaryResponse(
    @Schema(description = "선택 지역 상세 정보")
    RegionDetailResponse detail,

    @Schema(description = "선택 지역 최근 7일 추이")
    List<MosquitoTrendResponse> trend
) {
}
