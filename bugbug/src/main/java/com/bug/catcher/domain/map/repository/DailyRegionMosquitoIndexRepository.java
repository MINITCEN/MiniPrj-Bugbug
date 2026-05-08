package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.DailyRegionMosquitoIndex;
import com.bug.catcher.domain.entity.Region;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DailyRegionMosquitoIndexRepository extends JpaRepository<DailyRegionMosquitoIndex, Long> {

  boolean existsByRegionAndIndexDate(Region region, LocalDate date);
  List<DailyRegionMosquitoIndex> findAllByIndexDate(LocalDate date);

  // 오늘 데이터가 없을 때 가장 최근(어제 등) 데이터를 찾는 용도
  @Query("SELECT d FROM DailyRegionMosquitoIndex d JOIN FETCH d.region " +
      "WHERE d.indexDate = (SELECT MAX(d2.indexDate) FROM DailyRegionMosquitoIndex d2)")
  List<DailyRegionMosquitoIndex> findAllByLatestDate();
}
