package com.bug.catcher.global.infra;

import com.bug.catcher.domain.map.dto.MosquitoApiResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MosquitoApiService {

  private final RestTemplate restTemplate;

  @Value("${seoul.api.key}")
  private String apiKey;

  public MosquitoApiResponse fetchTodayMosquitoStatus(String date) {
    // 1. 서울 공공데이터 URL 조립
    String url = String.format("http://openapi.seoul.go.kr:8088/%s/json/MosquitoStatus/1/1/%s",
        apiKey, date);

    try {
      // 2. API 호출
      MosquitoApiResponse response = restTemplate.getForObject(url, MosquitoApiResponse.class);

      // 빈 값이 아니면 JSON응답을 DTO에 저장
      if (response != null && response.getMosquitoStatus() != null
          && !response.getMosquitoStatus().getList().isEmpty()) {
        return response;
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  // 전 날 데이터를 저장하는 메서드
  // 날짜에 -1을 해서 fetchTodayMosquitoStatus 재호출
  public MosquitoApiResponse fetchWithFallback(LocalDate date) {
    MosquitoApiResponse response = fetchTodayMosquitoStatus(date.toString());
    if (response == null) {
      log.info("fetchWithFallback: {} 데이터가 없어 전날({}) 데이터를 조회합니다.", date, date.minusDays(1));
      response = fetchTodayMosquitoStatus(date.minusDays(1).toString());
    }
    return response;
  }
}
