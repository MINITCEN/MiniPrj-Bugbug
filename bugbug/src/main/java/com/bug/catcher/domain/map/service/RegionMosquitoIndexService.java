package com.bug.catcher.domain.map.service;

import com.bug.catcher.domain.entity.Region;
import org.springframework.stereotype.Service;

@Service
public class RegionMosquitoIndexService {

  public static double calculateDistrictIndex(
      Region region,
      double seoulWaterIndex,
      double seoulResidentialIndex,
      double seoulParkIndex
  ) {
    double districtIndex = (seoulWaterIndex * region.getWaterRatio())
        + (seoulResidentialIndex * region.getResidentialRatio())
        + (seoulParkIndex * region.getParkRatio());

    double roundedIndex = Math.round(districtIndex * 10) / 10.0;
    return Math.min(roundedIndex, 100);
  }
}
