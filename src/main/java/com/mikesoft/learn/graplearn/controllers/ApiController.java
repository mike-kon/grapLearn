package com.mikesoft.learn.graplearn.controllers;

import com.mikesoft.learn.graplearn.worker.Orchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ApiController {

  private final Orchestrator orchestrator;

  @PostMapping("onestart")
  public String oneStart() {
    log.info("run one start");
    orchestrator.onsRun();
    return "Call one run";
  }

  @PostMapping("start")
  public String start() {
    log.info("start");
    orchestrator.start();
    return "Start";
  }

  @PostMapping("stop")
  public String stop() {
    log.info("stop");
    orchestrator.stop();
    return "Stop";
  }

  @PostMapping("wait")
  public String callWait(@RequestParam Integer waitTimes) {
    log.info("wait {} times", waitTimes);
    orchestrator.callWait(waitTimes);
    return "Add Wait %d times".formatted(waitTimes);
  }

  @PostMapping("crash")
  public String callCrash(@RequestParam Integer crashTimes) {
    log.info("crash {} times", crashTimes);
    orchestrator.crash(crashTimes);
    return "Add crash %d times".formatted(crashTimes);
  }
}
