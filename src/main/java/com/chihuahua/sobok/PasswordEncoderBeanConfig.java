package com.chihuahua.sobok;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// memberService -> SecurityConfig로의 순환 참조 문제를 해결하기 위해 별도의 클래스로 분리
@Configuration
public class PasswordEncoderBeanConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
