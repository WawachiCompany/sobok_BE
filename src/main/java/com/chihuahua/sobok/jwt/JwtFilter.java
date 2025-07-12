package com.chihuahua.sobok.jwt;

import com.chihuahua.sobok.member.MyUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;


public class JwtFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      Cookie[] cookies = request.getCookies();
      if (cookies == null) {
        filterChain.doFilter(request, response);
        return;
      }

      String jwtCookie = Arrays.stream(cookies)
          .filter(cookie -> "accessToken".equals(cookie.getName()))
          .map(Cookie::getValue)
          .findFirst()
          .orElse("");

      if (jwtCookie.isEmpty()) {
        filterChain.doFilter(request, response);
        return;
      }

      Claims claim = JwtUtil.extractToken(jwtCookie);
      if (claim == null) {
        filterChain.doFilter(request, response);
        return;
      }

      String loginType = claim.get("loginType", String.class);
      String provider = claim.get("provider", String.class);

      MyUserDetailsService.CustomUser customUser;

      // 소셜 로그인인 경우
      if ("native".equals(loginType)) {
        if ("apple".equals(provider) || "kakao".equals(provider) || "google".equals(provider)) {
          // 소셜 로그인의 경우 subject를 username으로 사용
          String socialId = claim.getSubject();
          customUser = new MyUserDetailsService.CustomUser(socialId, "");
        } else {
          // 알 수 없는 소셜 로그인 제공자
          filterChain.doFilter(request, response);
          return;
        }
      }
      // 일반 로그인인 경우
      else if ("normal".equals(loginType) && "local".equals(provider)) {
        String username = claim.get("username", String.class);
        if (username == null) {
          filterChain.doFilter(request, response);
          return;
        }
        customUser = new MyUserDetailsService.CustomUser(username, "");
      }
      // 알 수 없는 로그인 타입
      else {
        filterChain.doFilter(request, response);
        return;
      }

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(customUser, null);

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("인증에 실패했습니다: " + e.getMessage());
    }
  }
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        Cookie[] cookies = request.getCookies();
//        if(cookies == null) {
//            filterChain.doFilter(request, response);
//            return; // 쿠키가 비어있으면 다음 필터로 패스
//        }
//        var jwtCookie = "";
//        for (Cookie cookie : cookies) {
//            if (cookie.getName().equals("accessToken")) {
//                jwtCookie = cookie.getValue();
//            }
//        }
//
//        Claims claim;
//        try {
//            claim = JwtUtil.extractToken(jwtCookie);
//
//        } catch (Exception e) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        var arr = claim.get("authorities").toString().split(",");
//        var authorities = Arrays.stream(arr)
//                .map(SimpleGrantedAuthority::new).toList();
//
//        var customUser = new MyUserDetailsService.CustomUser(
//                claim.get("username").toString(),
//                "none",
//                authorities
//        );
//        var authToken = new UsernamePasswordAuthenticationToken(
//                customUser, ""
//        );
//        authToken.setDetails(new WebAuthenticationDetailsSource()
//                .buildDetails(request)
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//        filterChain.doFilter(request, response);
//    }

}
