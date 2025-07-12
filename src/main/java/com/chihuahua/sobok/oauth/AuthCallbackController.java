package com.chihuahua.sobok.oauth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Deprecated
@Controller
public class AuthCallbackController {

  @GetMapping("/auth/callback/success")
  public String authSuccess(@RequestParam String accessToken,
      @RequestParam String refreshToken,
      Model model) {
    model.addAttribute("accessToken", accessToken);
    model.addAttribute("refreshToken", refreshToken);
    return "auth-success"; // auth-success.html 템플릿 반환
  }
}
