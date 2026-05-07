package com.bug.catcher.global.scheduler;

import com.bug.catcher.domain.map.service.MosquitoIndexService;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MosquitoScheduler {
  private final MosquitoIndexService mosquitoIndexService;

  // 서버가 뜨자마자 이 메서드가 자동으로 실행됩니다.
  @PostConstruct
  public void init() {
    log.info("테스트를 위해 수동으로 로직을 호출합니다.");
    runDailyMosquitoCollection();
  }

  // 매일 오전 6시마다 모기지수가 업데이트 되게 스케줄링 했습니다.
  @Scheduled(cron = "0 0 6 * * *")
  public void runDailyMosquitoCollection() {
    mosquitoIndexService.calculateAndSaveDailyIndex(LocalDate.now());
  }
}
