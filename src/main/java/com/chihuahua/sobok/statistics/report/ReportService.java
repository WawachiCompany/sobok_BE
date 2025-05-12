package com.chihuahua.sobok.statistics.report;

import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.account.AccountLog;
import com.chihuahua.sobok.account.AccountLogRepository;
import com.chihuahua.sobok.account.AccountRepository;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineLog;
import com.chihuahua.sobok.routine.RoutineLogRepository;
import com.chihuahua.sobok.routine.RoutineRepository;
import com.chihuahua.sobok.routine.todo.TodoLog;
import com.chihuahua.sobok.routine.todo.TodoLogRepository;
import com.chihuahua.sobok.statistics.DailyAchieve;
import com.chihuahua.sobok.statistics.DailyAchieveDto;
import com.chihuahua.sobok.statistics.DailyAchieveRepository;
import com.chihuahua.sobok.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    private final TodoLogRepository todoLogRepository;

    public ResponseEntity<?> getReport(String yearMonth) {
        YearMonth yearMonthObj = YearMonth.parse(yearMonth);
        Member member = memberService.getMember();
        String startDate = yearMonthObj.atDay(1).toString();
        String endDate = yearMonthObj.atEndOfMonth().toString();
        Optional<MonthlyUserReport> monthlyUserReportOptional = monthlyUserReportRepository.findByMemberIdAndTargetYearMonth(member.getId(), yearMonthObj.toString());
        if(monthlyUserReportOptional.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "해당 월의 통계가 존재하지 않습니다."));
        }
        MonthlyUserReport monthlyUserReport = monthlyUserReportOptional.get();
        Map<String, Object> response = new HashMap<>();

        // 리포트 메세지에서의 재사용을 위해 변수로 저장
        List<DailyAchieveDto> result = statisticsService.getDailyAchieve(member, startDate, endDate); // 3페이지를 위한 데이터
        Long totalAccumulatedTime = monthlyUserReport.getTotalAccumulatedTime();
        String mostPerformedStartTime = getMostPerformedStartTime(member, yearMonthObj);
        Long totalAchievedCount = getTotalAchievedCount(result);



        response.put("totalTime", totalAccumulatedTime); // 1페이지 총 누적시간
        response.put("totalTimePercent", getTotalTimePercent(member, yearMonthObj)); // 1페이지 누적시간 순위
        response.put("routineStatistics", getMonthlyRoutineStatistics(member, yearMonthObj)); // 1페이지 루틴별 누적시간
        response.put("accountStatistics", getMonthlyAccountStatistics(member, yearMonthObj)); // 1페이지 적금별 누적시간
        response.put("reportMessage1", getReportMessage1(totalAccumulatedTime)); // 1페이지 리포트 메세지

        response.put("averageTime", monthlyUserReport.getAverageAccumulatedTime()); // 2페이지 하루 평균시간
        response.put("averageTimeCompare", getAverageTimeCompare(member, yearMonthObj)); // 2페이지 하루 평균시간 전체 평균 대비
        response.put("mostPerformedStartTime", mostPerformedStartTime); // 2페이지 가장 많이 진행한 시간대(30분 단위)
        if(!mostPerformedStartTime.equals("none")) {
            response.put("reportMessage2", getReportMessage2(LocalTime.parse(mostPerformedStartTime), member.getDisplayName())); // 2페이지 리포트 메세지
        }

        response.put("totalAchievedCount", totalAchievedCount); // 3페이지 총 달성 일자
        response.put("totalAchievedPercent", getTotalAchievedPercent(result)); // 3페이지 달성 일자 비율(눈 예보 정확도)
        response.put("dailyAchieve", result); // 3페이지 일별 달성 상태(캘린더 표시)
        response.put("reportMessage3", getReportMessage3(totalAchievedCount,yearMonthObj.getMonthValue())); // 3페이지 리포트 메세지

        response.put("consecutiveAchieveCount", calculateMonthlyConsecutiveAchieve(getDailyAchievesForCurrentMonth(member, yearMonthObj))); // 4페이지 월별 연속 달성일
        response.put("mostAchievedAccount", getMonthlyMostAchievedAccount(member, yearMonthObj)); // 5페이지 가장 많이 달성한 적금 {title, duration}

        return ResponseEntity.ok(response);
    }

    public double getTotalTimePercent(Member member, YearMonth yearMonth){
        String currentMonth = yearMonth.toString();
        List<MonthlyUserReport> reports = monthlyUserReportRepository.findAll().stream()
                .filter(report -> report.getTargetYearMonth().equals(currentMonth))
                .sorted((r1, r2) -> Long.compare(r2.getTotalAccumulatedTime(), r1.getTotalAccumulatedTime()))
                .toList();

        Optional<MonthlyUserReport> optionalReport = monthlyUserReportRepository.findByMemberIdAndTargetYearMonth(member.getId(), currentMonth);
        if (optionalReport.isEmpty()) {
            return 0.0; // 해당 월의 통계가 존재하지 않으면 0%로 처리
        }
        MonthlyUserReport memberReport = optionalReport.get();
        long memberTime = memberReport.getTotalAccumulatedTime();
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

    public long getAverageTimeCompare(Member member, YearMonth yearMonth) {
        String currentMonth = yearMonth.toString();
        long averageTime = Math.round(monthlyUserReportRepository.findAll().stream()
                .mapToLong(MonthlyUserReport::getAverageAccumulatedTime)
                .average().orElse(0));

        MonthlyUserReport report = monthlyUserReportRepository.findByMemberIdAndTargetYearMonth(member.getId(), currentMonth)
                .orElseThrow(() -> new IllegalStateException("해당 월의 통계가 존재하지 않습니다."));

        return report.getAverageAccumulatedTime() - averageTime;
    }


    public String getMostPerformedStartTime(Member member, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetWeen(member, startOfMonth, endOfMonth);

        // 30분 단위로 묶어서 카운트 (시간 부분만 사용)
        Map<LocalTime, Long> timeCount = todoLogs.stream()
                .map(todoLog -> roundToNearestHalfHour(todoLog.getStartTime().toLocalTime()))
                .collect(Collectors.groupingBy(time -> time, Collectors.counting()));

        return timeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().toString())
                .orElse("none");
    }

    public static LocalTime roundToNearestHalfHour(LocalTime time) {
        int minute = time.getMinute();
        if (minute < 15) {
            return time.truncatedTo(ChronoUnit.HOURS);
        } else if (minute < 45) {
            return time.truncatedTo(ChronoUnit.HOURS).plusMinutes(30);
        } else {
            return time.truncatedTo(ChronoUnit.HOURS).plusHours(1);
        }
    }

    public List<Map<String, Object>> getMonthlyRoutineStatistics(Member member, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Routine> routines = routineRepository.findByMember(member);
        return routines.stream()
                .map(routine -> {
                    long duration = routineLogRepository.findAllByRoutineAndIsCompletedAndEndTimeBetween(routine, true, startOfMonth, endOfMonth).stream()
                            .mapToLong(RoutineLog::getDuration)
                            .sum();
                    if(duration == 0) return null;
                    Map<String, Object> routineMap = new HashMap<>();
                    routineMap.put("title", routine.getTitle());
                    routineMap.put("duration", duration);
                    return routineMap;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMonthlyAccountStatistics(Member member, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Account> accounts = accountRepository.findByMember(member);
        return accounts.stream()
                .map(account -> {
                    long duration = accountLogRepository.findByAccountAndCreatedAtBetween(account, startOfMonth, endOfMonth).stream()
                            .mapToLong(AccountLog::getDepositTime)
                            .sum();
                    if(duration == 0) return null;
                    Map<String, Object> accountMap = new HashMap<>();
                    accountMap.put("title", account.getTitle());
                    accountMap.put("duration", duration);
                    return accountMap;
                })
                .filter(Objects::nonNull)
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
    public List<DailyAchieveDto> getDailyAchievesForCurrentMonth(Member member, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<DailyAchieve> dailyAchieves = dailyAchieveRepository.findByMemberAndDateBetween(member, startDate, endDate);
        return dailyAchieves.stream().map(this::convertToDto).toList();
    }

    public DailyAchieveDto convertToDto(DailyAchieve dailyAchieve) {
        DailyAchieveDto dto = new DailyAchieveDto();
        dto.setDate(dailyAchieve.getDate().toString());
        dto.setStatus(dailyAchieve.getStatus());
        return dto;
    }

    public Map<String, Object> getMonthlyMostAchievedAccount(Member member, YearMonth yearMonth) {
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

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

    public String getReportMessage1(Long totalAccumulatedTime) {
        if(totalAccumulatedTime >= 3600) {
            return "세상에에! 폭설이에요! 세상이 온통 하얀색이네요!";
        } else if(totalAccumulatedTime >= 2400) {
            return "눈이 정말 많이 내렸어요! 눈사람 여러개를 만들 수 있을 정도로요!";
        } else if(totalAccumulatedTime >= 1200) {
            return "이번 달은 눈 녹을 걱정이 없었어요! 대단해요!";
        } else if(totalAccumulatedTime >= 600) {
            return "조금씩 내리는 눈이 예뻤던 한달이에요! 눈 내리는 날이 얼마나 소중했는지!";
        } else {
            return "이번 달은 따뜻했나봐요! 다음 달에는 더 많은 눈이 내리겠죠 ?";
        }
    }

    public String getReportMessage2(LocalTime mostPerformedStartTime, String displayName) {
        if(isBetween(mostPerformedStartTime, LocalTime.of(6, 0), LocalTime.of(11, 59))) {
            return displayName + "님 덕분에 아침부터 예쁜 눈을 볼 수 있었어요!";
        }
        else if(isBetween(mostPerformedStartTime, LocalTime.of(12, 0), LocalTime.of(17, 59))) {
            return "나른한 오후, " + displayName + "님 덕분에 눈이 내리네요! 소소한 기분 전환, 최고에요!";
        }
        else if(isBetween(mostPerformedStartTime, LocalTime.of(18, 0), LocalTime.of(23, 59))) {
            return "저녁까지 힘차게 눈이 내리네요! 오늘도 수고하셨습니다!";
        }
        else {
            return "고요한 밤에 예쁜 눈이 내리네요! " + displayName + "님의 노력이 조용히 쌓이고 있군요!";
        }
    }

    public boolean isBetween(LocalTime time, LocalTime startTime, LocalTime endTime) {
        return (time.equals(startTime) || time.isAfter(startTime)) && time.isBefore(endTime);
    }

    public String getReportMessage3(Long totalAchievedCount, int currentMonth) {
        if(totalAchievedCount >= 21) {
            return "매일 눈이 내렸던 "+ currentMonth + "월이라니! 대단해요!!";
        }
        else if(totalAchievedCount >= 11) {
            return "눈이 꾸준히 내렸던 " + currentMonth + "월이네요! 수고하셨어요!";
        }
        else if(totalAchievedCount >= 6) {
            return "눈 내리는 날이 소중했던 " + currentMonth + "월이네요! 소중한 날들을 모아모아!";
        }
        else {
            return "맑은 날이 많았던 " + currentMonth + "월이네요! 눈 내리는 날이 많아졌으면 좋겠어요:)";
        }
    }



}
