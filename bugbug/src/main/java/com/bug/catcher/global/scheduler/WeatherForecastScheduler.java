package com.bug.catcher.global.scheduler;

import com.bug.catcher.domain.map.service.RegionWeatherForecastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeatherForecastScheduler {

  private final RegionWeatherForecastService regionWeatherForecastService;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("Application is ready. Starting weather forecast collection.");
    regionWeatherForecastService.updateLatestForecasts();
  }
  //API 제공 시간(~이후) : 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
  @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")
  public void runWeatherForecastCollection() {
    regionWeatherForecastService.updateLatestForecasts();
  }
}
