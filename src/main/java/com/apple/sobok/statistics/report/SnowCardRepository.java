package com.apple.sobok.statistics.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SnowCardRepository extends JpaRepository<SnowCard, Long> {
    List<SnowCard> findAllByMemberId(Long memberId);
}
