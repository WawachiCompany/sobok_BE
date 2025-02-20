package com.apple.sobok.account;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByMemberAndIsExpired(Member member, Boolean isExpired);

    Account findByMemberAndId(Member member, Long id);

    List<Account> findByMemberAndIsValidAndIsExpired(Member member, Boolean isValid, Boolean isExpired);

    List<Account> findByIsExpired(Boolean isExpired);

    List<Account> findByMember(Member member);

}
