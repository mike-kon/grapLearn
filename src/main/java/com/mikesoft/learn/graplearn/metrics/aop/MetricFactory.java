package com.mikesoft.learn.graplearn.metrics.aop;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
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
}

