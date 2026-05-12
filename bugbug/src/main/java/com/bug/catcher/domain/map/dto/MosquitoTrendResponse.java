package com.bug.catcher.domain.map.dto;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "지역별 최근 모기 지수 추이 응답")
public record MosquitoTrendResponse(

    @Schema(description = "지수 기준 일자", example = "2026-05-11")
    LocalDate date,

    @Schema(description = "모기 지수", example = "63.0")
    Double index
) {
    public static MosquitoTrendResponse from(DailyRegionMosquitoIndex entity) {
        return new MosquitoTrendResponse(
            entity.getIndexDate(),
            entity.getMosquitoIndex()
        );
    }
}
