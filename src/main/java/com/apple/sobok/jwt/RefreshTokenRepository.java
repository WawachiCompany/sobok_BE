package com.apple.sobok.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByRefreshToken(String refreshToken);
}
