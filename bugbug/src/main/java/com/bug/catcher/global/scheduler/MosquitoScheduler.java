package com.bug.catcher.global.scheduler;

import com.bug.catcher.domain.map.service.MosquitoIndexService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MosquitoScheduler {

  private final MosquitoIndexService mosquitoIndexService;

  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    log.info("Application is ready. Starting mosquito index backfill.");
    mosquitoIndexService.backfillRecentDays(7);
  }

  @Scheduled(cron = "0 0 6 * * *")
  public void runDailyMosquitoCollection() {
    mosquitoIndexService.calculateAndSaveDailyIndex(LocalDate.now());
  }
}
