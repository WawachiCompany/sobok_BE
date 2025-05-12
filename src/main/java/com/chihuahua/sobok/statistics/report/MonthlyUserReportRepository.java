package com.chihuahua.sobok.statistics.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MonthlyUserReportRepository extends JpaRepository<MonthlyUserReport, Long> {

    Optional<MonthlyUserReport> findByMemberIdAndTargetYearMonth(Long memberId, String yearMonth);
}
