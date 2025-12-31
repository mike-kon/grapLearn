package com.mikesoft.learn.graplearn.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app")
@Component
@Getter
@Setter
public class AppProperties {

  /**
   * Время быстрой обработки.
   */
  private Long shortWait;

  /**
   * Время долгой обработки.
   */
  private Long longWait;

  /**
   * Время задержки между запусками.
   */
  private Long delayTime;

}
