package com.chihuahua.sobok.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class AppleOAuth2Config {

    @Value("${social-login.provider.apple.team-id}")
    private String teamId;

    @Value("${social-login.provider.apple.client-id}")
    private String clientId;

    @Value("${social-login.provider.apple.key-id}")
    private String keyId;

    String keyPath = System.getenv("APPLE_PRIVATE_KEY_PATH");
    private final String privateKey;

    public AppleOAuth2Config() {
        try {
            this.privateKey = Files.readString(Paths.get(keyPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Apple private key file", e);
        }
    }


    @Bean("appleClientSecret")
    public String appleClientSecret() throws Exception {
        // privateKey는 PEM 형식의 전체 내용을 포함해야 하며, 필요하다면 개행 문자를 복원합니다.
        return AppleClientSecretGenerator.generateClientSecret(teamId, clientId, keyId, privateKey);
    }
}
