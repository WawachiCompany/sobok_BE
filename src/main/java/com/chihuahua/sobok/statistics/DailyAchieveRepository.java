package com.chihuahua.sobok.statistics;

import com.chihuahua.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyAchieveRepository extends JpaRepository<DailyAchieve, Long> {
    List<DailyAchieve> findByMemberAndDateBetween(Member member, LocalDate dateAfter, LocalDate dateBefore);
}
