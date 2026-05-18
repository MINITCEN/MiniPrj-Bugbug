package com.bug.catcher.domain.map.service;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.entity.RegionWeatherForecast;
import com.bug.catcher.domain.map.dto.MosquitoMapPageDto;
import com.bug.catcher.domain.map.dto.MosquitoRegionSummaryResponse;
import com.bug.catcher.domain.map.dto.MosquitoResponse;
import com.bug.catcher.domain.map.dto.MosquitoTrendResponse;
import com.bug.catcher.domain.map.dto.RegionDetailResponse;
import com.bug.catcher.domain.map.repository.DailyRegionMosquitoIndexRepository;
import com.bug.catcher.domain.map.repository.RegionRepository;
import com.bug.catcher.domain.map.repository.RegionWeatherForecastRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MosquitoMapQueryService {

  private static final String DEFAULT_GEO_JSON_URL = "/map/seoul-districts.geojson";

  private final DailyRegionMosquitoIndexRepository dailyRegionMosquitoIndexRepository;
  private final RegionRepository regionRepository;
  private final RegionWeatherForecastRepository regionWeatherForecastRepository;

  public MosquitoMapPageDto getPageData() {
    List<MosquitoResponse> regions = getCurrentStatuses();
    Long selectedRegionId = regions.stream()
        .map(MosquitoResponse::regionId)
        .filter(regionId -> regionId != null)
        .findFirst()
        .orElse(null);

    MosquitoRegionSummaryResponse initialSummary = selectedRegionId != null
        ? getRegionSummary(selectedRegionId)
        : new MosquitoRegionSummaryResponse(
            RegionDetailResponse.from("지역 선택", null, null),
            Collections.emptyList()
        );

    return new MosquitoMapPageDto(
        regions,
        selectedRegionId,
        initialSummary.detail(),
        initialSummary.trend(),
        DEFAULT_GEO_JSON_URL
    );
  }

  public List<MosquitoResponse> getCurrentStatuses() {
    LocalDate today = LocalDate.now();
    List<DailyRegionMosquitoIndex> entities = dailyRegionMosquitoIndexRepository.findAllByIndexDate(today);
    boolean isOldData = false;

    if (entities.isEmpty()) {
      entities = dailyRegionMosquitoIndexRepository.findAllByLatestDate();
      isOldData = true;
    }

    if (entities.isEmpty()) {
      return Collections.emptyList();
    }

    final boolean finalIsOldData = isOldData;
    return entities.stream()
        .map(entity -> MosquitoResponse.from(entity, finalIsOldData))
        .toList();
  }

  public MosquitoRegionSummaryResponse getRegionSummary(Long regionId) {
    Region region = regionRepository.findById(regionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다."));

    DailyRegionMosquitoIndex mosquitoIndex = dailyRegionMosquitoIndexRepository
        .findByRegionAndIndexDate(region, LocalDate.now())
        .or(() -> dailyRegionMosquitoIndexRepository.findTopByRegionOrderByIndexDateDesc(region))
        .orElse(null);

    Optional<RegionWeatherForecast> weatherForecast = regionWeatherForecastRepository.findByRegion(region);

    RegionDetailResponse detail = RegionDetailResponse.from(
        region.getName(),
        mosquitoIndex,
        weatherForecast.orElse(null)
    );

    List<MosquitoTrendResponse> trend = dailyRegionMosquitoIndexRepository
        .findTop7ByRegionOrderByIndexDateDesc(region)
        .stream()
        .map(MosquitoTrendResponse::from)
        .sorted(Comparator.comparing(MosquitoTrendResponse::date))
        .toList();

    return new MosquitoRegionSummaryResponse(detail, trend);
  }
}
