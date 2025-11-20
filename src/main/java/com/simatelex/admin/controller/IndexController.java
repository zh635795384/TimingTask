package com.simatelex.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author Nancy Zhao
 * @Date 2025/08/14 10:33
 * @Version 1.0
 **/
@Controller
public class IndexController {
    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("welcome")
    public String welcome() {
        return "welcome";
    }
}
