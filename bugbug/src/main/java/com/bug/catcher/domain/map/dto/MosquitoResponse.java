package com.bug.catcher.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "지역별 모기 지수 및 상태 응답 정보")
public record MosquitoResponse(

    @Schema(description = "지역 명칭", example = "강남구")
    String location,

    @Schema(description = "모기 지수 (0 ~ 100)", example = "75.5")
    Double index,

    @Schema(description = "활동 단계 (LOW, NORMAL, CAUTION, DANGER)", example = "CAUTION")
    String status,

    @Schema(description = "사용자 안내 메시지", example = "[현재 데이터 수집 중입니다. 완료 전까지 전일 데이터가 제공됩니다.]")
    String message
) {
  // 필요한 경우 추가적인 생성자나 로직을 record 내부에 작성할 수 있습니다.
}