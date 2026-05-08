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
import java.time.LocalDateTime;
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
            name = "uk_region_weather_forecast_region_forecast_at",
            columnNames = {"region_id", "forecast_at"}
        )
    }
)
public class RegionWeatherForecast {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "region_weather_forecast_id")
  private Long id;

  // Region that this forecast belongs to.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  // Forecast timestamp that will be shown to the user.
  @Column(name = "forecast_at", nullable = false)
  private LocalDateTime forecastAt;

  // 시간당 온도
  @Column(name = "temperature")
  private Double temperature;

  // 습도
  @Column(name = "humidity")
  private Integer humidity;

  // 강수량(mm)
  @Column(name = "precipitation")
  private Double precipitation;

  // 강수 타입
  @Column(name = "precipitation_type", length = 20)
  private String precipitationType;

  // 하늘 상태
  @Column(name = "sky_status", length = 20)
  private String skyStatus;

  // 풍속
  @Column(name = "wind_speed")
  private Double windSpeed;
}
