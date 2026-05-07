package com.bug.catcher.domain.map.service;

import com.bug.catcher.domain.entity.Region;
import org.springframework.stereotype.Service;

@Service
public class RegionMosquitoIndexService {

  public double calculateDistrictIndex(
      Region region,
      double seoulWaterIndex,
      double seoulResidentialIndex,
      double seoulParkIndex
  ) {
    double districtIndex = (seoulWaterIndex * region.getWaterRatio())
        + (seoulResidentialIndex * region.getResidentialRatio())
        + (seoulParkIndex * region.getParkRatio());
    return Math.min(districtIndex, 100);
  }
}
