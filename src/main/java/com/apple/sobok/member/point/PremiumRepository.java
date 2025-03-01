package com.apple.sobok.member.point;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PremiumRepository extends JpaRepository<Premium, Long> {
    List<Premium> findByMember(Member member);

    Optional<Premium> findByMemberAndEndAtAfter(Member member, LocalDate endAtBefore);
}
