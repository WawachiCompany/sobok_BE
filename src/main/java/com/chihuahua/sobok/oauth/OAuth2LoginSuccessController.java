package com.chihuahua.sobok.oauth;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class OAuth2LoginSuccessController {


    @GetMapping("/loginSuccess")
    public ResponseEntity<String> loginSuccess() {
        return ResponseEntity.ok("login success");
    }
}
