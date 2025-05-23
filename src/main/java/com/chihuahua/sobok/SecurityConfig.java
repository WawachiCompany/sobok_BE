package com.chihuahua.sobok;

import com.chihuahua.sobok.jwt.JwtFilter;
import com.chihuahua.sobok.oauth.AppleAuthorizationRequestResolver;
import com.chihuahua.sobok.oauth.CustomOAuth2UserService;
import com.chihuahua.sobok.oauth.OAuth2LoginSuccessHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Getter
@Setter
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Lazy
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 기본 등록 URI는 보통 "/oauth2/authorization"입니다.
        OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver =
                new AppleAuthorizationRequestResolver("/oauth2/authorization", clientRegistrationRepository);

        http.csrf(AbstractHttpConfigurer::disable); // CSRF 보안 기능 비활성화

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        ); // 세션 생성 정책을 STATELESS로 변경(JWT 토큰 사용)

        http.addFilterBefore(new JwtFilter(), ExceptionTranslationFilter.class);

        http
                .securityContext(securityContext -> securityContext.securityContextRepository(new HttpSessionSecurityContextRepository())) // 🔥 SecurityContext 유지
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOAuth2UserService) // 사용자 정보를 처리할 CustomOAuth2UserService
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 시 핸들러
                        .failureUrl("/login?error=true") // 로그인 실패 시 URL
                        .authorizationEndpoint(authorizationEndpoint ->
                                // 커스텀 리졸버를 지정하여 Apple 로그인 요청에 대해 response_mode 파라미터를 추가합니다.
                                authorizationEndpoint.authorizationRequestResolver(customAuthorizationRequestResolver)
                        )
                );

        return http.build();
    }

}
