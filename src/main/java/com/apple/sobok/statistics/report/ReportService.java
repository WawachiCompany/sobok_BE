package com.apple.sobok.statistics.report;

import com.apple.sobok.account.Account;
import com.apple.sobok.account.AccountLog;
import com.apple.sobok.account.AccountLogRepository;
import com.apple.sobok.account.AccountRepository;
import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineLog;
import com.apple.sobok.routine.RoutineLogRepository;
import com.apple.sobok.routine.RoutineRepository;
import com.apple.sobok.statistics.DailyAchieveDto;
import com.apple.sobok.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberService memberService;
    private final MonthlyUserReportRepository monthlyUserReportRepository;
    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;
    private final AccountRepository accountRepository;
    private final AccountLogRepository accountLogRepository;
    private final StatisticsService statisticsService;

    public ResponseEntity<?> getFirstPage() {
        Member member = memberService.getMember();
        String startDate = YearMonth.now().atDay(1).toString();
        String endDate = YearMonth.now().atEndOfMonth().toString();
        MonthlyUserReport monthlyUserReport = monthlyUserReportRepository.findByMemberIdAndYearMonth(member.getId(), YearMonth.now().toString());
        Map<String, Object> response = new HashMap<>();
        response.put("totalTime", monthlyUserReport.getTotalAccumulatedTime()); // 1페이지 총 누적시간
        response.put("totalTimePercent", getTotalTimePercent(member)); // 1페이지 누적시간 순위
        response.put("averageTime", monthlyUserReport.getAverageAccumulatedTime()); // 2페이지 하루 평균시간
        response.put("averageTimePercent", getAverageTimeCompare(member)); // 2페이지 하루 평균시간 전체 평균 대비
        response.put("routineStatistics", getMonthlyRoutineStatistics(member)); // 1페이지 루틴별 누적시간
        response.put("accountStatistics", getMonthlyAccountStatistics(member)); // 1페이지 적금별 누적시간
        List<DailyAchieveDto> result = statisticsService.getDailyAchieve(member, startDate, endDate);
        response.put("totalAchievedCount", getTotalAchievedCount(result)); // 3페이지 총 달성 일자









        return ResponseEntity.ok(response);
    }

    public int getTotalTimePercent(Member member){
        String currentMonth = YearMonth.now().toString();
        List<MonthlyUserReport> reports = monthlyUserReportRepository.findAll().stream()
                .filter(report -> report.getYearMonth().equals(currentMonth))
                .sorted((r1, r2) -> Long.compare(r2.getTotalAccumulatedTime(), r1.getTotalAccumulatedTime()))
                .toList();

        long memberTime = monthlyUserReportRepository.findByMemberIdAndYearMonth(member.getId(), currentMonth).getTotalAccumulatedTime();
        int rank = 0;
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).getTotalAccumulatedTime() == memberTime) {
                rank = i + 1;
                break;
            }
        }
        return (int) (((double) rank / reports.size()) * 100);
    }

    public long getAverageTimeCompare(Member member){
        String currentMonth = YearMonth.now().toString();
        long averageTime = Math.round(monthlyUserReportRepository.findAll().stream()
                .mapToLong(MonthlyUserReport::getAverageAccumulatedTime)
                .average().orElse(0));
        long memberTime = monthlyUserReportRepository.findByMemberIdAndYearMonth(member.getId(), currentMonth).getAverageAccumulatedTime();
        return memberTime - averageTime;
    }

    public List<Map<String, Object>> getMonthlyRoutineStatistics(Member member) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Routine> routines = routineRepository.findByMember(member);
        return routines.stream()
                .map(routine -> {
                    Map<String, Object> routineMap = new HashMap<>();
                    routineMap.put("title", routine.getTitle());
                    long duration = routineLogRepository.findAllByRoutineAndIsCompletedAndEndTimeBetween(routine, true, startOfMonth, endOfMonth).stream()
                            .mapToLong(RoutineLog::getDuration)
                            .sum();
                    routineMap.put("duration", duration);
                    return routineMap;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMonthlyAccountStatistics(Member member) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Account> accounts = accountRepository.findByMember(member);
        return accounts.stream()
                .map(account -> {
                    Map<String, Object> accountMap = new HashMap<>();
                    accountMap.put("title", account.getTitle());
                    long duration = accountLogRepository.findByAccountAndCreatedAtBetween(account, startOfMonth, endOfMonth).stream()
                            .mapToLong(AccountLog::getDepositTime)
                            .sum();
                    accountMap.put("duration", duration);
                    return accountMap;
                })
                .collect(Collectors.toList());
    }

    public long getTotalAchievedCount(List<DailyAchieveDto> result){
        return result.stream()
                .filter(dailyAchieveDto -> Objects.equals(dailyAchieveDto.getStatus(), "ALL_ACHIEVED") || Objects.equals(dailyAchieveDto.getStatus(), "SOME_ACHIEVED"))
                .count();

    }


}
