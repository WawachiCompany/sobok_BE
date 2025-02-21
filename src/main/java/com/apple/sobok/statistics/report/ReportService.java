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
import com.apple.sobok.statistics.DailyAchieve;
import com.apple.sobok.statistics.DailyAchieveDto;
import com.apple.sobok.statistics.DailyAchieveRepository;
import com.apple.sobok.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
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
    private final DailyAchieveRepository dailyAchieveRepository;

    public ResponseEntity<?> getReport() {
        Member member = memberService.getMember();
        String startDate = YearMonth.now().atDay(1).toString();
        String endDate = YearMonth.now().atEndOfMonth().toString();
        MonthlyUserReport monthlyUserReport = monthlyUserReportRepository.findByMemberIdAndYearMonth(member.getId(), YearMonth.now().toString());
        Map<String, Object> response = new HashMap<>();
        response.put("totalTime", monthlyUserReport.getTotalAccumulatedTime()); // 1페이지 총 누적시간
        response.put("totalTimePercent", getTotalTimePercent(member)); // 1페이지 누적시간 순위
        response.put("routineStatistics", getMonthlyRoutineStatistics(member)); // 1페이지 루틴별 누적시간
        response.put("accountStatistics", getMonthlyAccountStatistics(member)); // 1페이지 적금별 누적시간

        response.put("averageTime", monthlyUserReport.getAverageAccumulatedTime()); // 2페이지 하루 평균시간
        response.put("averageTimeCompare", getAverageTimeCompare(member)); // 2페이지 하루 평균시간 전체 평균 대비



        List<DailyAchieveDto> result = statisticsService.getDailyAchieve(member, startDate, endDate); // 3페이지를 위한 데이터
        response.put("totalAchievedCount", getTotalAchievedCount(result)); // 3페이지 총 달성 일자
        response.put("totalAchievedPercent", getTotalAchievedPercent(result)); // 3페이지 달성 일자 비율(눈 예보 정확도)
        response.put("dailyAchieve", result); // 3페이지 일별 달성 상태(캘린더 표시)

        response.put("consecutiveAchieveCount", calculateMonthlyConsecutiveAchieve(getDailyAchievesForCurrentMonth(member))); // 4페이지 월별 연속 달성일
        response.put("mostAchievedAccount", getMonthlyMostAchievedAccount(member)); // 5페이지 가장 많이 달성한 적금 {title, duration}

        return ResponseEntity.ok(response);
    }

    public double getTotalTimePercent(Member member){
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
        double percent = (double) rank / reports.size() * 100;
        return Math.round(percent * 10) / 10.0;
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

    public double getTotalAchievedPercent(List<DailyAchieveDto> result){
        var totalRoutineDays = result.stream() // DailyAchieveDto에서 실제 루틴이 들어있던 날짜 수
                .filter(dailyAchieveDto -> Objects.equals(dailyAchieveDto.getStatus(), "ALL_ACHIEVED") || Objects.equals(dailyAchieveDto.getStatus(), "SOME_ACHIEVED") || Objects.equals(dailyAchieveDto.getStatus(), "NONE_ACHIEVED"))
                .count();
        double percent = (double) getTotalAchievedCount(result) / totalRoutineDays * 100;
        return Math.round(percent * 10) / 10.0;
    }


    // 월별 연속 달성일 계산
    public int calculateMonthlyConsecutiveAchieve(List<DailyAchieveDto> dailyAchieves) {
        int streak = 0;
        int maxStreak = 0;

        // "ALL_ACHIEVED" 또는 "SOME_ACHIEVED"를 달성으로 간주.
        for (DailyAchieveDto dto : dailyAchieves) {
            if ("ALL_ACHIEVED".equals(dto.getStatus()) || "SOME_ACHIEVED".equals(dto.getStatus())) {
                streak++;
            } else if ("NO_ROUTINE".equals(dto.getStatus())) {
                continue;
            } else {
                // 달성하지 않은 날이 있으면 streak를 초기화
                streak = 0;
            }
            if (streak > maxStreak) {
                maxStreak = streak;
            }
        }
        return maxStreak;
    }

    // 월별 루틴 달성현황 출력(월별 연속 달성일 계산용)
    public List<DailyAchieveDto> getDailyAchievesForCurrentMonth(Member member) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();
        List<DailyAchieve> dailyAchieves = dailyAchieveRepository.findByMemberAndDateBetween(member, startDate, endDate);
        return dailyAchieves.stream().map(this::convertToDto).toList();
    }

    public DailyAchieveDto convertToDto(DailyAchieve dailyAchieve) {
        DailyAchieveDto dto = new DailyAchieveDto();
        dto.setDate(dailyAchieve.getDate().toString());
        dto.setStatus(dailyAchieve.getStatus());
        return dto;
    }

    public Map<String, Object> getMonthlyMostAchievedAccount(Member member) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Account> accounts = accountRepository.findByMember(member);
        Account mostAchievedAccount = accounts.stream()
                .max((a1, a2) -> {
                    long duration1 = accountLogRepository.findByAccountAndCreatedAtBetween(a1, startOfMonth, endOfMonth).stream()
                            .mapToLong(AccountLog::getDepositTime)
                            .sum();
                    long duration2 = accountLogRepository.findByAccountAndCreatedAtBetween(a2, startOfMonth, endOfMonth).stream()
                            .mapToLong(AccountLog::getDepositTime)
                            .sum();
                    return Long.compare(duration1, duration2);
                })
                .orElseThrow(() -> new IllegalArgumentException("적금이 존재하지 않습니다."));

        long duration = accountLogRepository.findByAccountAndCreatedAtBetween(mostAchievedAccount, startOfMonth, endOfMonth).stream()
                .mapToLong(AccountLog::getDepositTime)
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("title", mostAchievedAccount.getTitle());
        result.put("duration", duration);
        return result;
    }


}
