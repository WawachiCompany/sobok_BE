package com.chihuahua.sobok.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByRefreshToken(String refreshToken);

    void deleteAllByExpiredAtBefore(Date expiredAtBefore);
}
