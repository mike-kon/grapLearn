package com.mikesoft.learn.graplearn.util;

  import org.springframework.lang.Contract;

  import java.util.function.Supplier;

public class Utils {

  private Utils() {}

  @Contract("null,_->_")
  public static <R> R safeNull(Supplier<R> supplier, R defaultValue) {
    try {
      return supplier.get();
    } catch (NullPointerException e) {
      return defaultValue;
    }
  }

  public static void powerfulWork(long millisecond) {
    try {
      Thread.sleep(millisecond);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
