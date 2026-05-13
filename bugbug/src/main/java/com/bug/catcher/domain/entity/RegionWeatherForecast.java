package com.bug.catcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "region_weather_forecast",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_region_weather_forecast_region",
            columnNames = {"region_id"}
        )
    }
)
public class RegionWeatherForecast {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "region_weather_forecast_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "base_time", nullable = false, length = 4)
  private String baseTime;

  @Column(name = "temperature")
  private Double temperature;

  @Column(name = "humidity")
  private Integer humidity;

  @Column(name = "precipitation", length = 30)
  private String precipitation;

  @Column(name = "precipitation_type", length = 20)
  private String precipitationType;

  @Column(name = "sky_status", length = 20)
  private String skyStatus;

  @Column(name = "wind_speed")
  private Double windSpeed;

  public void updateForecast(
      LocalDate baseDate,
      String baseTime,
      Double temperature,
      Integer humidity,
      String precipitation,
      String precipitationType,
      String skyStatus,
      Double windSpeed
  ) {
    this.baseDate = baseDate;
    this.baseTime = baseTime;
    this.temperature = temperature;
    this.humidity = humidity;
    this.precipitation = precipitation;
    this.precipitationType = precipitationType;
    this.skyStatus = skyStatus;
    this.windSpeed = windSpeed;
  }
}
