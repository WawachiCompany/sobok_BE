package com.chihuahua.sobok.Firebase;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByMemberId(Long memberId);

    Optional<FcmToken> findByMemberIdAndFcmToken(Long memberId, String fcmToken);
}
