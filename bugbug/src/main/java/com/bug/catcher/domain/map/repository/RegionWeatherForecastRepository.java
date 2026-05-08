package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.entity.RegionWeatherForecast;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionWeatherForecastRepository extends JpaRepository<RegionWeatherForecast, Long> {

  boolean existsByRegionAndForecastAt(Region region, LocalDateTime forecastAt);

  List<RegionWeatherForecast> findAllByRegionIdAndForecastAtBetweenOrderByForecastAtAsc(
      Long regionId,
      LocalDateTime start,
      LocalDateTime end
  );
}
