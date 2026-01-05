package com.mikesoft.learn.graplearn.metrics.aop;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricFactory {

  private final MeterRegistry metricRegistry;

  @Contract("null -> null")
  public Counter createCounter(String name) {
    return name != null
        ? Counter.builder(name).register(metricRegistry)
        : null;
  }

  @Contract("null -> null")
  public Timer createTimer(String name) {
    return name != null
        ? Timer.builder(name).register(metricRegistry)
        : null;
  }

  @Contract("null,_ -> null")
  public Gauge getMetricGauge(String name, Supplier<? extends Number> fix) {
    return name != null
        ? Gauge.builder(name, fix).register(metricRegistry)
        : null;
  }

  @Contract("null,_ -> null; _,null -> fail")
  public Timer createHistogram(String name, List<Long> quantiles) {
    if (quantiles == null || quantiles.isEmpty()) {
      throw new IllegalArgumentException("quantile must not be null or empty");
    }

    Duration[] durations = quantiles.stream()
        .map(Duration::ofMillis)
        .toArray(Duration[]::new);
    Timer ret = name != null
        ? Timer
        .builder(name)
        .sla(durations)
        .register(metricRegistry)
        : null;
    if (ret != null) {
      log.info("Created metric histogram with name {}", name);
    }
    return ret;
  }
}

