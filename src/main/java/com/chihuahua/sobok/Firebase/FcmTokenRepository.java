package com.chihuahua.sobok.Firebase;

import com.chihuahua.sobok.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByMember(Member member);

  Optional<FcmToken> findByMemberId(Long memberId);

  Optional<FcmToken> findByMemberAndFcmToken(Member member, String fcmToken);
}
