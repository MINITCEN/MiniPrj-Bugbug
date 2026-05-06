package com.bug.catcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "region")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Region {

  @Id
  @Column(name = "region_id")
  private Long id; // 행정구역코드

  @Column(nullable = false, unique = true)
  private String name; // 자치구명

  // 토지 이용 비율 (0.0 ~ 1.0, 세 컬럼의 합은 1.0이어야 함)
  @Column(name = "water_ratio", nullable = false)
  private Double waterRatio; // 수변부 비율

  @Column(name = "residential_ratio", nullable = false)
  private Double residentialRatio; // 주거지 비율

  @Column(name = "park_ratio", nullable = false)
  private Double parkRatio; // 공원 비율

  @Builder
  public Region(Long id, String name, Double waterRatio, Double residentialRatio, Double parkRatio) {
    this.id = id;
    this.name = name;
    this.waterRatio = waterRatio;
    this.residentialRatio = residentialRatio;
    this.parkRatio = parkRatio;
  }

  /**
   * 자치구별 모기 지수 계산 로직
   * @param seoulWaterIndex 서울시 수변부 모기 지수
   * @param seoulResIndex 서울시 주거지 모기 지수
   * @param seoulParkIndex 서울시 공원 모기 지수
   * @return 해당 자치구의 최종 보정 지수
   */
  public double calculateDistrictIndex(double seoulWaterIndex, double seoulResIndex, double seoulParkIndex) {
    double idx = (seoulWaterIndex * this.waterRatio) +
        (seoulResIndex * this.residentialRatio) +
        (seoulParkIndex * this.parkRatio);
    if(idx>100){
      return 100;
    }else{
      return idx;
    }
  }
}
