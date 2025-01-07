package com.apple.sobok;

import com.apple.sobok.oauth.CustomOAuth2UserService;
import com.apple.sobok.oauth.OAuth2LoginSuccessHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Getter
@Setter
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable); // CSRF 보안 기능 비활성화

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ); // 세션 생성 정책을 STATELESS로 변경(JWT 토큰 사용)

        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 커스텀 로그인 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOAuth2UserService) // 사용자 정보를 처리할 CustomOAuth2UserService
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 시 핸들러
                        .failureUrl("/login?error=true") // 로그인 실패 시 URL
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}