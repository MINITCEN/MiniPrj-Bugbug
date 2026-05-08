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

  // Region that this weather snapshot belongs to.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "region_id", nullable = false)
  private Region region;

  // Base date from the KMA forecast response.
  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  // Base time from the KMA forecast response.
  @Column(name = "base_time", nullable = false, length = 4)
  private String baseTime;

  // Forecast temperature in Celsius.
  @Column(name = "temperature")
  private Double temperature;

  // Forecast humidity in percent.
  @Column(name = "humidity")
  private Integer humidity;

  // Forecast precipitation text from the KMA response.
  @Column(name = "precipitation", length = 30)
  private String precipitation;

  // Precipitation state such as none, rain, rain/snow, or snow.
  @Column(name = "precipitation_type", length = 20)
  private String precipitationType;

  // Sky state such as clear, mostly cloudy, or cloudy.
  @Column(name = "sky_status", length = 20)
  private String skyStatus;

  // Forecast wind speed in meters per second.
  @Column(name = "wind_speed")
  private Double windSpeed;
}
