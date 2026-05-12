package com.bug.catcher.domain.map.service;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.map.dto.MosquitoApiResponse;
import com.bug.catcher.domain.map.repository.DailyRegionMosquitoIndexRepository;
import com.bug.catcher.domain.map.repository.RegionRepository;
import com.bug.catcher.global.infra.MosquitoApiService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MosquitoIndexService {

  private final MosquitoApiService mosquitoApiService;
  private final RegionRepository regionRepository;
  private final DailyRegionMosquitoIndexRepository indexRepository;

  // 메서드 로직 너무 길어서 추후 리팩토링 예정.
  // 저장, 조회 로직 분리 예정
  // 어제꺼 저장되어 있는 지 먼저 조회하고 있으면 반환
  @Transactional
  public void calculateAndSaveDailyIndex(LocalDate date){
    // 모기지수 API 가져오기 (fetchTodayMosquitoStatus에서 RestTemplate 이용하여 JSON 받아옵니다)
    MosquitoApiResponse apiData = mosquitoApiService.fetchTodayMosquitoStatus(date.toString());

    if (apiData == null || apiData.getMosquitoStatus() == null || apiData.getMosquitoStatus().getList().isEmpty()) {
      log.info("{} 날짜의 데이터가 존재하지 않아 전날 데이터를 조회를 시도합니다.", date);
      date = date.minusDays(1);
      apiData = mosquitoApiService.fetchWithFallback(date);
    }

    // Region 테이블을 리스트에 담아와서 오늘의 모기지수를 계산하여 DailyRegionMosquitoIndex에 저장합니다.
    List<Region> regionList = regionRepository.findAll();

    if (regionList.isEmpty()) {
      log.warn("No regions found. Skipping daily mosquito index save for {}.", date);
      return;
    }

    // DTO의 모기지수 값들을 미리 꺼내놓음
    var firstData = apiData.getMosquitoStatus().getList().get(0);
    double waterValue = firstData.getWaterValue();
    double houseValue = firstData.getHouseValue();
    double parkValue = firstData.getParkValue();

    // 반복문 돌면서 지역별 모기지수 테이블에 저장.
    for(Region region : regionList){
      if (indexRepository.existsByRegionAndIndexDate(region, date)){
        log.info("이미 존재하는 날짜의 값입니다.");
        continue;
      }
      double finalIndex = RegionMosquitoIndexService.calculateDistrictIndex(
          region, waterValue, houseValue, parkValue
      );
      DailyRegionMosquitoIndex dailyIndex = DailyRegionMosquitoIndex.builder()
          .region(region)
          .indexDate(date)
          .mosquitoIndex(finalIndex)
          .build();

      indexRepository.save(dailyIndex);
    }
    log.info("{} 자치구별 모기 지수 생성 및 저장 완료", date);
  }

  @Transactional
  public void backfillRecentDays(int days) {
    LocalDate today = LocalDate.now();
    for (int offset = days - 1; offset >= 0; offset--) {
      calculateAndSaveDailyIndex(today.minusDays(offset));
    }
  }

}
