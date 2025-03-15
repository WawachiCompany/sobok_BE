package com.chihuahua.sobok.account;

import com.chihuahua.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByMemberAndIsExpired(Member member, Boolean isExpired);

    List<Account> findByMemberAndIsExpiredAndIsEnded(Member member, Boolean isExpired, Boolean isEnded);

    Account findByMemberAndId(Member member, Long id);

    List<Account> findByMemberAndIsValidAndIsExpired(Member member, Boolean isValid, Boolean isExpired);

    List<Account> findByIsExpired(Boolean isExpired);

    List<Account> findByMember(Member member);

}
