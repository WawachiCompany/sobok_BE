package com.apple.sobok.member.point;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PremiumRepository extends JpaRepository<Premium, Long> {
    Optional<Premium> findByMember(Member member);
}
