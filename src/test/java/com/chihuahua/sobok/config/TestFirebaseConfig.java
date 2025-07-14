package com.chihuahua.sobok.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestFirebaseConfig {

  @Bean
  @Primary
  public Object firebaseApp() {
    // 테스트 환경에서는 Firebase를 사용하지 않으므로 Mock 객체 반환
    return new Object();
  }
}
