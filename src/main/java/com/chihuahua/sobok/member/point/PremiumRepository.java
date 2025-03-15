package com.chihuahua.sobok.member.point;

import com.chihuahua.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PremiumRepository extends JpaRepository<Premium, Long> {
    List<Premium> findByMember(Member member);

    Optional<Premium> findByMemberAndEndAtAfter(Member member, LocalDate endAtBefore);
}
