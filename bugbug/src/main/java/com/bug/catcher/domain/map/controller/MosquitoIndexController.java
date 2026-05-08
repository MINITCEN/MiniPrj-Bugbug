package com.bug.catcher.domain.map.controller;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.entity.RegionWeatherForecast;
import com.bug.catcher.domain.map.dto.MosquitoResponse;
import com.bug.catcher.domain.map.dto.RegionDetailResponse;
import com.bug.catcher.domain.map.repository.DailyRegionMosquitoIndexRepository;
import com.bug.catcher.domain.map.repository.RegionRepository;
import com.bug.catcher.domain.map.repository.RegionWeatherForecastRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Mosquito Status", description = "모기 지수 및 지역 상세 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mosquito")
public class MosquitoIndexController {

  private final DailyRegionMosquitoIndexRepository dailyRegionMosquitoIndexRepository;
  private final RegionRepository regionRepository;
  private final RegionWeatherForecastRepository regionWeatherForecastRepository;

  @GetMapping("/current")
  public ResponseEntity<List<MosquitoResponse>> getAllCurrentStatus() {
    LocalDate today = LocalDate.now();
    List<DailyRegionMosquitoIndex> entities = dailyRegionMosquitoIndexRepository.findAllByIndexDate(today);
    boolean isOldData = false;

    if (entities.isEmpty()) {
      entities = dailyRegionMosquitoIndexRepository.findAllByLatestDate();
      isOldData = true;
    }

    if (entities.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    final boolean finalIsOldData = isOldData;

    return ResponseEntity.ok(
        entities.stream()
            .map(entity -> MosquitoResponse.from(entity, finalIsOldData))
            .toList()
    );
  }

  @GetMapping("/detail/{regionId}")
  public ResponseEntity<RegionDetailResponse> getRegionDetail(@PathVariable Long regionId) {
    Region region = regionRepository.findById(regionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다."));

    DailyRegionMosquitoIndex mosquitoIndex = dailyRegionMosquitoIndexRepository
        .findByRegionAndIndexDate(region, LocalDate.now())
        .or(() -> dailyRegionMosquitoIndexRepository.findTopByRegionOrderByIndexDateDesc(region))
        .orElse(null);

    Optional<RegionWeatherForecast> weatherForecast = regionWeatherForecastRepository.findByRegion(region);

    if (mosquitoIndex == null && weatherForecast.isEmpty()) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(
        RegionDetailResponse.from(region.getName(), mosquitoIndex, weatherForecast.orElse(null))
    );
  }
}
