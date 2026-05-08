package com.bug.catcher.domain.map.controller;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.map.dto.MosquitoResponse;
import com.bug.catcher.domain.map.repository.DailyRegionMosquitoIndexRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mosquito Status", description = "모기 현황 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mosquito")
public class MosquitoIndexController {

  private final DailyRegionMosquitoIndexRepository dailyRegionMosquitoIndexRepository;

  @GetMapping("/current")
  public ResponseEntity<List<MosquitoResponse>> getAllCurrentStatus(){
    LocalDate today = LocalDate.now();

    // 1. 우선 오늘 날짜로 조회
    List<DailyRegionMosquitoIndex> entities = dailyRegionMosquitoIndexRepository.findAllByIndexDate(today);
    boolean isOldData = false;

    // 2. 오늘 데이터가 없으면 전날(가장 최근) 데이터 조회
    if (entities.isEmpty()) {
      entities = dailyRegionMosquitoIndexRepository.findAllByLatestDate();
      isOldData = true; // 전날 데이터임을 표시
    }

    // 3. 여전히 데이터가 아예 없다면 204 반환
    if (entities.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    final boolean finalIsOldData = isOldData; // 람다식 내 사용을 위한 final 변수
    List<MosquitoResponse> responses = entities.stream()
        .map(entity -> {
          // 전날 데이터 여부에 따라 메시지 생성
          String message ="";
          if (finalIsOldData) {
            message = "[현재 데이터 수집 중입니다. 완료 전까지 전일 데이터가 제공됩니다.]";
          }

          return new MosquitoResponse(
              entity.getRegion().getName(), // 지역 이름 (ex.강남구)
              entity.getMosquitoIndex(), // 모기지수 (ex. 45)
              calculateStatus(entity.getMosquitoIndex()), // 모기지수 상태 (ex. 관심)
              message // 전 날 데이터인 경우의 메시지 ([현재 데이터 수집 중입니다. 완료 전까지 전일 데이터가 제공됩니다.])
          );
        })
        .toList();
    return ResponseEntity.ok(responses);
  }
  private String calculateStatus(Double index) {
    if (index >= 75) return "불쾌";
    if (index >= 50) return "주의";
    if (index >= 25) return "관심";
    return "쾌적";
  }
}
