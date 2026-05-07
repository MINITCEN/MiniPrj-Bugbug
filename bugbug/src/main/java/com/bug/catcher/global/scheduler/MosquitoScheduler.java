package com.bug.catcher.global.scheduler;

import com.bug.catcher.domain.map.service.MosquitoIndexService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MosquitoScheduler {
  private final MosquitoIndexService mosquitoIndexService;

  // 매일 오전 6시마다 모기지수가 업데이트 되게 스케줄링 했습니다.
  @Scheduled(cron = "0 0 6 * * *")
  public void run() {
    mosquitoIndexService.calculateAndSaveDailyIndex(LocalDate.now());
  }
}
