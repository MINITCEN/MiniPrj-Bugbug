package com.bug.catcher.domain.map.repository;

import com.bug.catcher.domain.entity.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

}
