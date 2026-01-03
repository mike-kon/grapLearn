package com.mikesoft.learn.graplearn.metrics.aop;

import com.mikesoft.learn.graplearn.configurations.MetricsProperties;
import com.mikesoft.learn.graplearn.metrics.annotation.MyMetric;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricAspect {

  private static final String METRIC_FORMAT = "%s_%s_%s";

  private final MetricsProperties metricsProperties;
  private final MetricFactory metricFactory;

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

    Counter fullMetric = createMetric(metric, metricsProperties.getTotalCounterName(),
        metricFactory::createCounter, Counter.class);
    Counter successMetric = createMetric(metric, metricsProperties.getSuccessCounterName(),
        metricFactory::createCounter, Counter.class);
    Counter failureMetric = createMetric(metric, metricsProperties.getFailureCounterName(),
        metricFactory::createCounter, Counter.class);
    Timer benchMetric = createMetric(metric, metricsProperties.getBenchmarkName(),
        metricFactory::createTimer, Timer.class);
    Gauge gaugeMetric = createMetric(metric, metricsProperties.getGaugeName(),
        x -> metricFactory.getMetricGauge(x, this::gaugeMeasure), Gauge.class);

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

  private <T extends Meter> T createMetric(MyMetric metric, String metricSuffix, Function<String, T> createMetric,
                                           Class<T> clazz) {
    String metricName = getMetricName(metric, metricSuffix);
    if (metricName == null) {
      return null;
    }
    if (usedMetrics.containsKey(metricName)) {
      Meter res = usedMetrics.get(metricName);
      return clazz.cast(res);
    } else {
      T newMetric = createMetric.apply(metricName);
      usedMetrics.put(metricName, newMetric);
      return newMetric;
    }
  }

  private Long gaugeMeasure() {
    Long e = gaugesValues.poll();
    return e != null ? e : 0L;
  }

  private String getMetricName(MyMetric metric, String suffix) {
    if (metric != null && metric.enabled() && Strings.isNotBlank(suffix)) {
      return METRIC_FORMAT.formatted(metricPrefix, metric.name(), suffix);
    } else {
      return null;
    }
  }

}
