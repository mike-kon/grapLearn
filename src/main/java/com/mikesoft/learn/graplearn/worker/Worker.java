package com.mikesoft.learn.graplearn.worker;

import com.mikesoft.learn.graplearn.configurations.AppProperties;
import com.mikesoft.learn.graplearn.excepions.BusinessException;
import com.mikesoft.learn.graplearn.metrics.annotation.MyMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.mikesoft.learn.graplearn.util.Utils.powerfulWork;

@Component
@RequiredArgsConstructor
@Slf4j
public class Worker {

  private final AppProperties appProperties;

  @Async
  @MyMetric(name = "worker")
  public void run(boolean longTime, boolean crash) {
    long sleep = longTime ? appProperties.getLongWait() : appProperties.getShortWait();
    log.info("worker start");
    powerfulWork(sleep);
    if (crash) {
      log.info("worker crashed");
      throw new BusinessException();
    }
    log.info("worker done");
  }
}
