package com.chihuahua.sobok.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Configuration
public class AppleOAuth2Config {

    @Value("${social-login.provider.apple.team-id}")
    private String teamId;

    @Value("${social-login.provider.apple.client-id}")
    private String clientId;

    @Value("${social-login.provider.apple.key-id}")
    private String keyId;

    private final String privateKey;

    public AppleOAuth2Config(Environment environment) {
        // 현재 활성화된 프로필을 확인
        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        Path keyPath;

        if (isProd) {
            // 프로덕션 환경
            keyPath = Paths.get("/opt/secrets/apple-authkey.p8");
        } else {
            // 개발 환경
            keyPath = Paths.get("src/main/resources/apple-authkey.p8");
        }

        if (!Files.exists(keyPath)) {
            throw new RuntimeException("Apple private key file does not exist at path: " + keyPath.toAbsolutePath());
        }

        try {
            this.privateKey = Files.readString(keyPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Apple private key file at path: " + keyPath.toAbsolutePath(), e);
        }
    }

    @Bean("appleClientSecret")
    public String appleClientSecret() throws Exception {
        // privateKey는 PEM 형식의 전체 내용을 포함해야 하며, 필요하다면 개행 문자를 복원합니다.
        return AppleClientSecretGenerator.generateClientSecret(teamId, clientId, keyId, privateKey);
    }
}

