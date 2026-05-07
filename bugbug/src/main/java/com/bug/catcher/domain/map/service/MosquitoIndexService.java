package com.bug.catcher.domain.map.service;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.map.dto.MosquitoApiResponse;
import com.bug.catcher.domain.map.repository.DailyRegionMosquitoIndexRepository;
import com.bug.catcher.domain.map.repository.RegionRepository;
import com.bug.catcher.global.infra.MosquitoApiService;
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

  public void calculateAndSaveDailyIndex(LocalDate date){
    // 모기지수 API 가져오기 (fetchTodayMosquitoStatus에서 RestTemplate 이용하여 JSON 받아옵니다)
    MosquitoApiResponse apiData = mosquitoApiService.fetchTodayMosquitoStatus(date.toString());

    if(apiData == null){
      log.info("DATA가 없습니다.");
      return;
    }

    // Region 테이블을 리스트에 담아와서 오늘의 모기지수를 계산하여 DailyRegionMosquitoIndex에 저장합니다.
    List<Region> regionList = regionRepository.findAll();

    for(Region region : regionList){
      if (indexRepository.existsByRegionAndIndexDate(region, date)) continue;
      double waterValue = apiData.getMosquitoStatus().getList().getFirst().getWaterValue();
      double houseValue = apiData.getMosquitoStatus().getList().getFirst().getHouseValue();
      double parkValue = apiData.getMosquitoStatus().getList().getFirst().getParkValue();
      double finalIndex = RegionMosquitoIndexService.calculateDistrictIndex(region, waterValue, houseValue, parkValue);
      DailyRegionMosquitoIndex dailyIndex = DailyRegionMosquitoIndex.builder()
          .region(region)
          .indexDate(date)
          .mosquitoIndex(finalIndex)
          .build();

      indexRepository.save(dailyIndex);
    }
    log.info("{} 자치구별 모기 지수 생성 및 저장 완료", date);
  }

}
