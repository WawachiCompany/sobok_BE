package com.chihuahua.sobok;

import com.chihuahua.sobok.jwt.JwtFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Getter
@Setter
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable); // CSRF 보안 기능 비활성화

    http.sessionManagement((session) -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    ); // 세션 생성 정책을 STATELESS로 변경(JWT 토큰 사용)

    http.addFilterBefore(new JwtFilter(), ExceptionTranslationFilter.class);

    http
        .securityContext(securityContext -> securityContext.securityContextRepository(
            new HttpSessionSecurityContextRepository())) // SecurityContext 유지
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/**", "/api/auth/**").permitAll()
            .requestMatchers("/user/login/jwt", "/user/refresh-token", "/user/create")
            .permitAll() // 로그인, 토큰 갱신, 회원가입
            .requestMatchers("/user/is-duplicated/**").permitAll() // 중복 체크
            .anyRequest().authenticated()
        );

    return http.build();
  }
}
