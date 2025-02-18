package com.apple.sobok.statistics;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DailyAchieveRepository extends JpaRepository<DailyAchieve, Long> {
    Optional<DailyAchieve> findByMember(Member member);
}
