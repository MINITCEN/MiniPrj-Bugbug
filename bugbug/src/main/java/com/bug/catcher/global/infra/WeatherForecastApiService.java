package com.bug.catcher.global.infra;

import com.bug.catcher.domain.map.dto.WeatherForecastApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherForecastApiService {

  private final RestTemplate restTemplate;

  @Value("${kma.api.key}")
  private String serviceKey;

  @Value("${kma.api.base-url}")
  private String baseUrl;

  public WeatherForecastApiResponse fetchVillageForecast(
      String baseDate,
      String baseTime,
      int nx,
      int ny
  ) {
    String url = UriComponentsBuilder.fromUriString(baseUrl)
        .path("/getVilageFcst")
        .queryParam("serviceKey", serviceKey)
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 300)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDate)
        .queryParam("base_time", baseTime)
        .queryParam("nx", nx)
        .queryParam("ny", ny)
        .build(false)
        .toUriString();

    try {
      WeatherForecastApiResponse response =
          restTemplate.getForObject(url, WeatherForecastApiResponse.class);

      if (response == null || response.getResponse() == null) {
        log.warn("날씨 API 응답이 비어있습니다. nx={}, ny={}, baseDate={}, baseTime={}",
            nx, ny, baseDate, baseTime);
        return null;
      }

      WeatherForecastApiResponse.Header header = response.getResponse().getHeader();
      if (header != null && !"00".equals(header.getResultCode())) {
        log.warn("날씨 API 응답 오류. code={}, msg={}",
            header.getResultCode(), header.getResultMsg());
      }

      return response;
    } catch (Exception e) {
      log.error("Failed to fetch weather forecast. nx={}, ny={}, baseDate={}, baseTime={}",
          nx, ny, baseDate, baseTime, e);
      return null;
    }
  }
}
