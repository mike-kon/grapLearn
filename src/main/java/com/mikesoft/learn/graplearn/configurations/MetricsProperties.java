package com.mikesoft.learn.graplearn.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.metric.suffix")
@Data
public class MetricsProperties {

  /**
   * Счетчики событий.
   */
  private String totalCounterName;

  /**
   * Счетчики успешных событий.
   */
  private String successCounterName;

  /**
   * Счетчики неуспешных событий.
   */
  private String failureCounterName;

  /**
   * Счетчики измерения времени выполнения типа Timer.
   */
  private String benchmarkName;

  /**
   * Счетчик времени выполнения типа Gauge.
   *
   */
  private String gaugeName;

  /**
   * Счетчик времени выполнения типа Histogram
   */
  private String histogramName;

  /**
   * Кванитли для гистограммы
   */
  List<Long> histogramQuantile;
}
