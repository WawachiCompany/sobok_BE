package com.apple.sobok.statistics.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SnowCardRepository extends JpaRepository<SnowCard, Long> {
    List<SnowCard> findAllByMemberId(Long memberId);

    Optional<SnowCard> findByMemberIdAndTargetYearMonth(Long memberId, String targetYearMonth);
}
