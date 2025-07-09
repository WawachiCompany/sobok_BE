package com.chihuahua.sobok.oauth;

import com.chihuahua.sobok.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthAccountRepository extends JpaRepository<OauthAccount, Long> {

  Optional<OauthAccount> findByOauthIdAndProvider(String oauthId, String provider);

  Optional<OauthAccount> findByMemberAndProvider(Member member, String provider);
}
