package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.Region;
import com.bug.catcher.domain.entity.RegionWeatherForecast;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionWeatherForecastRepository extends JpaRepository<RegionWeatherForecast, Long> {

  Optional<RegionWeatherForecast> findByRegion(Region region);
}
