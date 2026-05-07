package com.bug.catcher.global.infra.mosquitoApi;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MosquitoApiService {

  private final RestTemplate restTemplate;

  @Value("${seoul.api.key}")
  private String apiKey;

  public MosquitoApiResponse.MosquitoData fetchTodayMosquitoStatus(String date) {
    // 1. URL 조립
    String url = String.format("http://openapi.seoul.go.kr:8088/%s/json/MosquitoStatus/1/1/%s",
        apiKey, date);

    try {
      // 2. API 호출
      MosquitoApiResponse response = restTemplate.getForObject(url, MosquitoApiResponse.class);

      if (response != null && response.getMosquitoStatus() != null
          && !response.getMosquitoStatus().getList().isEmpty()) {
        return response.getMosquitoStatus().getList().getFirst();
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return null;
  }
}
