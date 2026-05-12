package com.bug.catcher.domain.map.dto;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "지역별 모기 지수 및 상태 응답 정보")
public record MosquitoResponse(

    @Schema(description = "지역 ID", example = "1168000000")
    Long regionId,

    @Schema(description = "지역명", example = "강남구")
    String location,

    @Schema(description = "모기 지수 (0 ~ 100)", example = "75.5")
    Double index,

    @Schema(description = "모기 활동 단계", example = "주의")
    String status,

    @Schema(description = "데이터 안내 메시지", example = "[현재 데이터 수집 중입니다. 완료 전까지 전일 데이터가 제공됩니다.]")
    String message
) {
    public static MosquitoResponse from(DailyRegionMosquitoIndex entity, boolean isOldData) {
        double idx = entity.getMosquitoIndex();
        String msg = isOldData ? "[현재 데이터 수집 중입니다. 완료 전까지 전일 데이터가 제공됩니다.]" : "";

        return new MosquitoResponse(
            entity.getRegion().getId(),
            entity.getRegion().getName(),
            idx,
            calculateStatus(idx),
            msg
        );
    }

    private static String calculateStatus(Double index) {
        if (index >= 75) return "불쾌";
        if (index >= 50) return "주의";
        if (index >= 25) return "관심";
        return "쾌적";
    }
}