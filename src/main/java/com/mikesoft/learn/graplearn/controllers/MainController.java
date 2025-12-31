package com.mikesoft.learn.graplearn.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.mikesoft.learn.graplearn.util.Constants.CRASH_COUNT_START;
import static com.mikesoft.learn.graplearn.util.Constants.NEED_LONG_COUNT_START;

@Controller()
@Slf4j
public class MainController {

  @GetMapping("/")
  public String index(Model model) {
    log.info("get file index");
    model.addAttribute("title", "Проверка работы графаны");
    model.addAttribute("waitTimes", NEED_LONG_COUNT_START);
    model.addAttribute("crashTimes", CRASH_COUNT_START);
    return "index";
  }

}
