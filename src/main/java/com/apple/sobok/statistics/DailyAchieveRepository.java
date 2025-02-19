package com.apple.sobok.statistics;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyAchieveRepository extends JpaRepository<DailyAchieve, Long> {
    List<DailyAchieve> findByMemberAndDateBetween(Member member, LocalDate dateAfter, LocalDate dateBefore);
}
