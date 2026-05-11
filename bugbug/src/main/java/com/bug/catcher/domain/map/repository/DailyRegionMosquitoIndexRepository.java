package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DailyRegionMosquitoIndexRepository extends JpaRepository<DailyRegionMosquitoIndex, Long> {

  boolean existsByRegionAndIndexDate(Region region, LocalDate date);

  Optional<DailyRegionMosquitoIndex> findByRegionAndIndexDate(Region region, LocalDate date);

  Optional<DailyRegionMosquitoIndex> findTopByRegionOrderByIndexDateDesc(Region region);

  @Query("SELECT d FROM DailyRegionMosquitoIndex d JOIN FETCH d.region WHERE d.indexDate = :date")
  List<DailyRegionMosquitoIndex> findAllByIndexDate(LocalDate date);

  List<DailyRegionMosquitoIndex> findTop7ByRegionOrderByIndexDateDesc(Region region);

  @Query("SELECT d FROM DailyRegionMosquitoIndex d JOIN FETCH d.region " +
      "WHERE d.indexDate = (SELECT MAX(d2.indexDate) FROM DailyRegionMosquitoIndex d2)")
  List<DailyRegionMosquitoIndex> findAllByLatestDate();
}
