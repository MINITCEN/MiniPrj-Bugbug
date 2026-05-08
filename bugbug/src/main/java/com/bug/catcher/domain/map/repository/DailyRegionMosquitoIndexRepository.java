package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRegionMosquitoIndexRepository extends JpaRepository<DailyRegionMosquitoIndex, Long> {

  boolean existsByRegionAndIndexDate(Region region, LocalDate date);
}
