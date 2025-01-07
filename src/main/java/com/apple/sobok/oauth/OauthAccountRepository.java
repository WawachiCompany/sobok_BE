package com.apple.sobok.oauth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {
    Optional<OauthAccount> findByOauthIdAndProvider(String oauthId, String provider);
}
