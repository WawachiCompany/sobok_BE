package com.chihuahua.sobok.oauth;

import com.chihuahua.sobok.jwt.JwtUtil;
import com.chihuahua.sobok.member.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtUtil jwtUtil;
  private final MemberService memberService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    String jwt = jwtUtil.createToken(authentication);
    String refreshToken = jwtUtil.createRefreshToken(authentication);

    memberService.setAccessCookie(response, jwt);
    memberService.setRefreshCookie(response, refreshToken);

    // 중간 페이지로 리다이렉트 (토큰을 URL에 직접 노출하지 않기 위해)
    String callbackUrl = String.format("/auth/callback/success?accessToken=%s&refreshToken=%s", jwt,
        refreshToken);

    response.sendRedirect(callbackUrl);
  }
}
