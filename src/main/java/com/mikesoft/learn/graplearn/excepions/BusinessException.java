package com.mikesoft.learn.graplearn.excepions;

public class BusinessException extends RuntimeException {

  @Override
  public String getMessage() {
    return "Business Error";
  }
}
