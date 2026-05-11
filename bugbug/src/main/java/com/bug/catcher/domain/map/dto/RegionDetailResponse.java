package com.bug.catcher.domain.map.dto;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.RegionWeatherForecast;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(description = "자치구 상세 정보 응답")
public record RegionDetailResponse(

    @Schema(description = "자치구명", example = "강남구")
    String regionName,

    @Schema(description = "모기 지수 기준 일자", example = "2026-05-08")
    LocalDate mosquitoIndexDate,

    @Schema(description = "모기 지수", example = "75.5")
    Double mosquitoIndex,

    @Schema(description = "모기 지수 상태", example = "주의")
    String mosquitoStatus,

    @Schema(description = "날씨 발표 일자", example = "2026-05-08")
    LocalDate weatherBaseDate,

    @Schema(description = "날씨 발표 시각", example = "1400")
    String weatherBaseTime,

    @Schema(description = "기온", example = "22.5")
    Double temperature,

    @Schema(description = "습도", example = "58")
    Integer humidity,

    @Schema(description = "강수량 원문", example = "강수없음")
    String precipitation,

    @Schema(description = "강수 형태", example = "없음")
    String precipitationType,

    @Schema(description = "하늘 상태", example = "맑음")
    String skyStatus,

    @Schema(description = "풍속", example = "2.4")
    Double windSpeed
) {

  public static RegionDetailResponse from(
      String regionName,
      DailyRegionMosquitoIndex mosquitoIndex,
      RegionWeatherForecast weatherForecast
  ) {
    return RegionDetailResponse.builder()
        .regionName(regionName)
        .mosquitoIndexDate(mosquitoIndex != null ? mosquitoIndex.getIndexDate() : null)
        .mosquitoIndex(mosquitoIndex != null ? mosquitoIndex.getMosquitoIndex() : null)
        .mosquitoStatus(mosquitoIndex != null ? calculateStatus(mosquitoIndex.getMosquitoIndex()) : null)
        .weatherBaseDate(weatherForecast != null ? weatherForecast.getBaseDate() : null)
        .weatherBaseTime(weatherForecast != null ? weatherForecast.getBaseTime() : null)
        .temperature(weatherForecast != null ? weatherForecast.getTemperature() : null)
        .humidity(weatherForecast != null ? weatherForecast.getHumidity() : null)
        .precipitation(weatherForecast != null ? weatherForecast.getPrecipitation() : null)
        .precipitationType(weatherForecast != null ? weatherForecast.getPrecipitationType() : null)
        .skyStatus(weatherForecast != null ? weatherForecast.getSkyStatus() : null)
        .windSpeed(weatherForecast != null ? weatherForecast.getWindSpeed() : null)
        .build();
  }

  private static String calculateStatus(Double index) {
    if (index == null) {
      return null;
    }
    if (index >= 75) {
      return "불쾌";
    }
    if (index >= 50) {
      return "주의";
    }
    if (index >= 25) {
      return "관심";
    }
    return "쾌적";
  }
}
