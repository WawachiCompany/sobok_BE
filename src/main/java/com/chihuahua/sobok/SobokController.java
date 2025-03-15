package com.chihuahua.sobok;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SobokController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        return "index";
    }
}
