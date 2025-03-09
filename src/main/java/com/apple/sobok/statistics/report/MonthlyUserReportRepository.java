package com.apple.sobok.statistics.report;

import org.springframework.data.jpa.repository.JpaRepository;


public interface MonthlyUserReportRepository extends JpaRepository<MonthlyUserReport, Long> {

    MonthlyUserReport findByMemberIdAndTargetYearMonth(Long memberId, String yearMonth);
}
