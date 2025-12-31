package com.mikesoft.learn.graplearn.metrics.aop;

import com.mikesoft.learn.graplearn.configurations.MetricsProperties;
import com.mikesoft.learn.graplearn.metrics.annotation.MyMetric;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Contract;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricAspect {

  private static final String METRIC_FORMAT = "%s_%s_%s";

  private final MetricsProperties metricsProperties;
  private final MeterRegistry metricRegistry;

  /**
   * Префикс метрик.
   */
  @Value("${app.metric.prefix}")
  private String metricPrefix;

  private final HashMap<String, Meter> usedMetrics = new HashMap<>();
  private final Queue<Long> gaugesValues = new ConcurrentLinkedQueue<>();

  @Around("@annotation(com.mikesoft.learn.graplearn.metrics.annotation.MyMetric)")
  public Object calcMetric(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature method = (MethodSignature) joinPoint.getSignature();
    MyMetric metric = method.getMethod().getAnnotation(MyMetric.class);

    Counter fullMetric = getMetric(getMetricName(metric, metricsProperties.getTotalCounterName()), Counter.class);
    Counter successMetric = getMetric(getMetricName(metric, metricsProperties.getSuccessCounterName()), Counter.class);
    Counter failureMetric = getMetric(getMetricName(metric, metricsProperties.getFailureCounterName()), Counter.class);
    Timer benchMetric = getMetric(getMetricName(metric, metricsProperties.getBenchmarkName()), Timer.class);
    Gauge gaugeMetric = getMetricGauge(getMetricName(metric, metricsProperties.getGaugeName()), this::gaugeMeasure);

    log.debug("Метрика для метода {}", method.getName());
    long start = benchMetric != null || gaugeMetric != null
        ? System.currentTimeMillis()
        : 0;

    if (fullMetric != null) {
      fullMetric.increment();
    }
    try {
      Object ret = joinPoint.proceed();
      if (successMetric != null) {
        successMetric.increment();
      }
      return ret;
    } catch (Throwable throwable) {
      if (failureMetric != null) {
        failureMetric.increment();
      }
      throw throwable;
    } finally {
      long execTime = benchMetric != null || gaugeMetric != null
          ? System.currentTimeMillis() - start
          : 0;
      if (benchMetric != null) {
        benchMetric.record(execTime, TimeUnit.MILLISECONDS);
      }
      if (gaugeMetric != null) {
        gaugesValues.add(execTime);
      }
    }
  }

  private Long gaugeMeasure() {
    Long e = gaugesValues.poll();
    if (e == null) {
      return 0L;
    }
    if (e.compareTo(0L) >= 0) {
      log.info("gauge used: {}", e);
    }
    return e;
  }

  @Contract("null,_->null")
  private <T extends Meter> T getMetric(String name, Class<T> meterType) {
    if (name == null) {
      return null;
    }
    Meter metric;
    if (usedMetrics.containsKey(name)) {
      metric = usedMetrics.get(name);
    } else {
      metric = getMetricType(name, meterType);
      usedMetrics.put(name, metric);
    }
    return meterType.cast(metric);
  }

  private <T extends Meter> Meter getMetricType(String name, Class<T> meterType) {
    if (meterType == Counter.class) {
      return Counter.builder(name).register(metricRegistry);
    } else if (meterType == Timer.class) {
      return Timer.builder(name).register(metricRegistry);
    } else {
      throw new UnsupportedOperationException(meterType.getSimpleName());
    }
  }

  @Contract("null,_ -> null")
  private Gauge getMetricGauge(String metricName, Supplier<? extends Number> fix) {
    if (metricName == null) {
      return null;
    }
    return Gauge.builder(metricName, fix).register(metricRegistry);
  }

  private String getMetricName(MyMetric metric, String suffix) {
    if (metric != null && Strings.isNotBlank(suffix)) {
      return METRIC_FORMAT.formatted(metricPrefix, metric.name(), suffix);
    } else {
      return null;
    }
  }
}
