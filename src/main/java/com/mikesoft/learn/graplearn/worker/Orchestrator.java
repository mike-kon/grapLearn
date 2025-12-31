package com.mikesoft.learn.graplearn.worker;

import com.mikesoft.learn.graplearn.configurations.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class Orchestrator {

  private final AppProperties appProperties;
  private final Worker worker;

  private final AtomicInteger needCrashCount = new AtomicInteger(0);
  private final AtomicInteger needLongCount = new AtomicInteger(0);

  private final AtomicBoolean running = new AtomicBoolean(false);

  public void onsRun() {
    callWorker();
  }

  public void start() {
    running.set(true);
  }

  public void stop() {
    running.set(false);
  }

  public void callWait(Integer waitTimes) {
    needLongCount.set(waitTimes);
  }

  public void crash(Integer crashTimes) {
    needCrashCount.set(crashTimes);
  }

  @Scheduled(fixedRateString = "${app.delay-time}")
  public void runWorker() {
    if (running.get()) {
      callWorker();
    }
  }

  private void callWorker() {
    boolean needLong = needLongCount.getAndUpdate(x -> x > 0 ? x - 1 : 0) > 0;
    boolean needCrash = needCrashCount.getAndUpdate(x -> x > 0 ? x - 1 : 0) > 0;
    worker.run(needLong, needCrash);
  }
}
