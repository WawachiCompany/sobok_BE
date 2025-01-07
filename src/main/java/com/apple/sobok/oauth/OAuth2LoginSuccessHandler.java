package com.apple.sobok.oauth;

import com.apple.sobok.jwt.JwtUtil;
import com.apple.sobok.member.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String jwt = jwtUtil.createToken(authentication);
        String refreshToken = jwtUtil.createRefreshToken(authentication);

        memberService.setCookie(response, refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwt);



        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(tokens));
    }
}
