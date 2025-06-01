package com.chihuahua.sobok.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

@Configuration
public class AppleOAuth2ClientConfig {

    private final String appleClientSecret;

    @Autowired
    public AppleOAuth2ClientConfig(@Qualifier("appleClientSecret") String appleClientSecret) {
        this.appleClientSecret = appleClientSecret;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.apple.client-id}") String clientId,
            @Value("${spring.security.oauth2.client.registration.apple.redirect-uri}") String redirectUri) {

        // 기존 코드와 동일
        ClientRegistration appleRegistration = ClientRegistration.withRegistrationId("apple")
                .clientId(clientId)
                .clientSecret(appleClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
                .scope("openid", "email", "name")
                .authorizationUri("https://appleid.apple.com/auth/authorize")
                .tokenUri("https://appleid.apple.com/auth/token")
                .jwkSetUri("https://appleid.apple.com/auth/keys")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .build();

        return new InMemoryClientRegistrationRepository(appleRegistration);
    }
}