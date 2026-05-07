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
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(name = "water_ratio", nullable = false)
  private Double waterRatio;

  @Column(name = "residential_ratio", nullable = false)
  private Double residentialRatio;

  @Column(name = "park_ratio", nullable = false)
  private Double parkRatio;

  @Builder
  public Region(Long id, String name, Double waterRatio, Double residentialRatio, Double parkRatio) {
    this.id = id;
    this.name = name;
    this.waterRatio = waterRatio;
    this.residentialRatio = residentialRatio;
    this.parkRatio = parkRatio;
  }
}
