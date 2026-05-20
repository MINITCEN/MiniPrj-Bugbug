package com.bug.catcher.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "모기지수 지도 페이지 초기 렌더링 데이터")
public record MosquitoMapPageDto(
    @Schema(description = "지역별 현재 모기지수 목록")
    List<MosquitoResponse> regions,

    @Schema(description = "초기 선택 지역 ID")
    Long selectedRegionId,

    @Schema(description = "초기 선택 지역 상세 정보")
    RegionDetailResponse initialDetail,

    @Schema(description = "초기 선택 지역 최근 7일 추이")
    List<MosquitoTrendResponse> initialTrend,

    @Schema(description = "지도 GeoJSON URL")
    String geoJsonUrl
) {
}
