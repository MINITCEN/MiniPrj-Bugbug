package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.Region;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

}
