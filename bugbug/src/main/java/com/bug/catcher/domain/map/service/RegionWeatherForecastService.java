package com.bug.catcher.domain.map.service;

import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.entity.RegionWeatherForecast;
import com.bug.catcher.domain.map.dto.WeatherForecastApiResponse;
import com.bug.catcher.domain.map.repository.RegionRepository;
import com.bug.catcher.domain.map.repository.RegionWeatherForecastRepository;
import com.bug.catcher.global.infra.WeatherForecastApiService;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionWeatherForecastService {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

  private static final List<BaseTimeSlot> BASE_TIME_SLOTS = List.of(
      new BaseTimeSlot(LocalTime.of(2, 10), "0200"),
      new BaseTimeSlot(LocalTime.of(5, 10), "0500"),
      new BaseTimeSlot(LocalTime.of(8, 10), "0800"),
      new BaseTimeSlot(LocalTime.of(11, 10), "1100"),
      new BaseTimeSlot(LocalTime.of(14, 10), "1400"),
      new BaseTimeSlot(LocalTime.of(17, 10), "1700"),
      new BaseTimeSlot(LocalTime.of(20, 10), "2000"),
      new BaseTimeSlot(LocalTime.of(23, 10), "2300")
  );

  private final WeatherForecastApiService weatherForecastApiService;
  private final RegionRepository regionRepository;
  private final RegionWeatherForecastRepository regionWeatherForecastRepository;

  @Transactional
  public void updateLatestForecasts() {
    BaseDateTime baseDateTime = resolveBaseDateTime(LocalDateTime.now());
    updateForecasts(baseDateTime.baseDate(), baseDateTime.baseTime());
  }

  @Transactional
  public void updateForecasts(LocalDate baseDate, String baseTime) {
    List<Region> regions = regionRepository.findAll();

    for (Region region : regions) {
      if (region.getGridX() == null || region.getGridY() == null) {
        log.warn("지역의 grid 좌표가 없습니다. regionId={}", region.getId());
        continue;
      }

      WeatherForecastApiResponse response = weatherForecastApiService.fetchVillageForecast(
          baseDate.format(DATE_FORMATTER),
          baseTime,
          region.getGridX(),
          region.getGridY()
      );

      if (response == null
          || response.getResponse() == null
          || response.getResponse().getBody() == null
          || response.getResponse().getBody().getItems() == null
          || response.getResponse().getBody().getItems().getItem().isEmpty()) {
        log.warn("예보 정보를 받아오지 못했습니다. regionId={}, baseDate={}, baseTime={}",
            region.getId(), baseDate, baseTime);
        continue;
      }

      // API 응답은 카테고리별(TMP, REH, SKY ...)로 분리되어 오므로
      // 같은 예보 시각(fcstDate + fcstTime) 기준으로 다시 묶는다.
      Map<LocalDateTime, ForecastAccumulator> forecastMap = new LinkedHashMap<>();

      for (WeatherForecastApiResponse.ForecastItem item : response.getResponse().getBody().getItems().getItem()) {
        LocalDateTime forecastAt = parseForecastDateTime(item.getForecastDate(), item.getForecastTime());
        ForecastAccumulator accumulator = forecastMap.computeIfAbsent(
            forecastAt,
            ignored -> new ForecastAccumulator()
        );
        applyCategory(accumulator, item.getCategory(), item.getForecastValue());
      }

      if (forecastMap.isEmpty()) {
        continue;
      }

      // 지역별 최신 1건만 저장하므로,
      // 이번 발표(baseDate/baseTime)에서 가장 첫 예보 슬롯만 대표값으로 사용한다.
      LocalDateTime nearestForecastAt = selectNearestForecastTime(forecastMap, LocalDateTime.now());
      ForecastAccumulator value = forecastMap.get(nearestForecastAt);

      regionWeatherForecastRepository.findByRegion(region)
          .ifPresentOrElse(
              forecast -> forecast.updateForecast(
                  baseDate,
                  baseTime,
                  value.temperature,
                  value.humidity,
                  value.precipitation,
                  value.precipitationType,
                  value.skyStatus,
                  value.windSpeed
              ),
              () -> regionWeatherForecastRepository.save(
                  RegionWeatherForecast.builder()
                      .region(region)
                      .baseDate(baseDate)
                      .baseTime(baseTime)
                      .temperature(value.temperature)
                      .humidity(value.humidity)
                      .precipitation(value.precipitation)
                      .precipitationType(value.precipitationType)
                      .skyStatus(value.skyStatus)
                      .windSpeed(value.windSpeed)
                      .build()
              )
          );
    }
    log.info("지역별 날씨 정보 저장 완료.");
  }

  BaseDateTime resolveBaseDateTime(LocalDateTime now) {
    LocalDate baseDate = now.toLocalDate();
    LocalTime currentTime = now.toLocalTime();

    // 현재 시각 기준으로 API 조회가 가능한 가장 최근 발표 시각을 찾는다.
    for (int i = BASE_TIME_SLOTS.size() - 1; i >= 0; i--) {
      BaseTimeSlot slot = BASE_TIME_SLOTS.get(i);
      if (!currentTime.isBefore(slot.availableAt())) {
        return new BaseDateTime(baseDate, slot.baseTime());
      }
    }

    // 02:10 이전에는 전날 23:00 발표분이 최신이다.
    return new BaseDateTime(baseDate.minusDays(1), "2300");
  }

  // fcstDate, fcstTime 문자열을 그룹핑용 시각 값으로 변환한다.
  private LocalDateTime parseForecastDateTime(String forecastDate, String forecastTime) {
    return LocalDateTime.of(
        LocalDate.parse(forecastDate, DATE_FORMATTER),
        LocalTime.parse(forecastTime, TIME_FORMATTER)
    );
  }

  private LocalDateTime selectNearestForecastTime(
      Map<LocalDateTime, ForecastAccumulator> forecastMap,
      LocalDateTime now
  ) {
    return forecastMap.keySet().stream()
        .min((left, right) -> {
          long leftDiff = Math.abs(Duration.between(now, left).toMinutes());
          long rightDiff = Math.abs(Duration.between(now, right).toMinutes());
          return Long.compare(leftDiff, rightDiff);
        })
        .orElseThrow();
  }

  // 카테고리별 응답 행을 하나의 예보 스냅샷 객체에 누적 반영한다.
  private void applyCategory(ForecastAccumulator accumulator, String category, String value) {
    if (category == null || value == null) {
      return;
    }

    switch (category) {
      case "TMP" -> accumulator.temperature = parseDouble(value);
      case "REH" -> accumulator.humidity = parseInteger(value);
      case "PCP" -> accumulator.precipitation = parsePrecipitation(value);
      case "PTY" -> accumulator.precipitationType = mapPrecipitationType(value);
      case "SKY" -> accumulator.skyStatus = mapSkyStatus(value);
      case "WSD" -> accumulator.windSpeed = parseDouble(value);
      default -> {
      }
    }
  }

  // 기온, 풍속처럼 단순 숫자 항목을 파싱한다.
  private Double parseDouble(String value) {
    try {
      return Double.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  // 습도(REH)는 정수형 퍼센트 값으로 내려온다.
  private Integer parseInteger(String value) {
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  // 강수량(PCP)은 텍스트 원문 그대로 저장한다.
  private String parsePrecipitation(String value) {
    return value == null ? null : value.trim();
  }

  // 기상청 PTY 코드를 읽기 쉬운 강수 형태 문자열로 바꾼다.
  private String mapPrecipitationType(String value) {
    return switch (value) {
      case "0" -> "없음";
      case "1" -> "비";
      case "2" -> "비/눈";
      case "3" -> "눈";
      case "4" -> "소나기";
      default -> value;
    };
  }

  // 기상청 SKY 코드를 읽기 쉬운 하늘 상태 문자열로 바꾼다.
  private String mapSkyStatus(String value) {
    return switch (value) {
      case "1" -> "맑음";
      case "3" -> "구름많음";
      case "4" -> "흐림";
      default -> value;
    };
  }

  private record BaseDateTime(LocalDate baseDate, String baseTime) {
  }

  // baseTime: 기상청 발표 시각
  // availableAt: 해당 발표분을 실제로 조회할 수 있는 시각
  private record BaseTimeSlot(LocalTime availableAt, String baseTime) {
  }

  // 카테고리별 응답을 하나의 대표 예보값으로 합칠 때 사용하는 임시 객체
  private static class ForecastAccumulator {
    private Double temperature;
    private Integer humidity;
    private String precipitation;
    private String precipitationType;
    private String skyStatus;
    private Double windSpeed;
  }
}
