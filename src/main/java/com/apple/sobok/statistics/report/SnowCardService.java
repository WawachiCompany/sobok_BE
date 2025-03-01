package com.apple.sobok.statistics.report;

import com.apple.sobok.member.Member;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineLog;
import com.apple.sobok.routine.RoutineLogRepository;
import com.apple.sobok.routine.todo.*;
import com.apple.sobok.statistics.DailyAchieveDto;
import com.apple.sobok.statistics.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SnowCardService {

    private final TodoRepository todoRepository;
    private final TodoLogRepository todoLogRepository;
    private final StatisticsService statisticsService;
    private final ReportService reportService;
    private final CategoryRepository categoryRepository;
    private final RoutineLogRepository routineLogRepository;

    // 눈 카드 달성 조건 확인 후 뽑기
    public Map<String, String> getSnowCard(Member member, String yearMonth) {
        YearMonth yearMonthObj = YearMonth.parse(yearMonth);
        List<String> snowCards = new ArrayList<>();
        if (isHexagon(member)) {
            snowCards.add("hexagon");
        }
        snowCards.add(getMostPerformedCategoryLastMonth(member, yearMonthObj));
        snowCards.add(getMoon(member, yearMonthObj));
        if (isLike(member, yearMonthObj)) {
            snowCards.add("like");
        }
        if (isRolyPoly(member, yearMonthObj)) {
            snowCards.add("rolyPoly");
        }
        if (isBeaker(member, yearMonthObj)) {
            snowCards.add("beaker");
        }
        if (isAngel(member, yearMonthObj)) {
            snowCards.add("angel");
        }
        if (ispudding(member, yearMonthObj)) {
            snowCards.add("pudding");
        }
        if (isFairy(member, yearMonthObj)) {
            snowCards.add("fairy");
        }
        if (isCrab(member, yearMonthObj)) {
            snowCards.add("crab");
        }
        if (isSpring(member)) {
            snowCards.add("spring");
        }
        Map<String, String> result = new HashMap<>();
        Random random = new Random();
        result.put("snowCard", snowCards.get(random.nextInt(snowCards.size())));
        return result;
    }

    // 육각형 모양의 눈 조각
    public boolean isHexagon(Member member) {
        List<Todo> todoList = todoRepository.findAllByMember(member);
        Set<String> categories = todoList.stream()
                .map(Todo::getCategory)
                .collect(Collectors.toSet());
        return categories.size() > 6;
    }

    // 카테고리 가장 많이 한거
    public String getMostPerformedCategoryLastMonth(Member member, YearMonth yearMonth) {
        LocalDateTime startOfLastMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<TodoLog> lastMonthTodoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetWeen(member, startOfLastMonth, endOfLastMonth);
        Map<String, Long> categoryCount = lastMonthTodoLogs.stream()
                .map(todoLog -> todoRepository.findById(todoLog.getTodo().getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Todo::getCategory, Collectors.counting()));

        return categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
    }

    // 달 모양의 눈 조각
    public String getMoon(Member member, YearMonth yearMonth) {
        List<DailyAchieveDto> result = statisticsService.getDailyAchieve(member, yearMonth.atDay(1).toString(), yearMonth.atEndOfMonth().toString());
        double percent = reportService.getTotalAchievedPercent(result);
        if (percent == 100) {
            return "full";
        } else if (percent >= 50) {
            return "half";
        } else if (percent >= 25) {
            return "quarter";
        } else {
            return "cloud";
        }
    }

    // 좋아하는 마음의 눈 조각
    public boolean isLike(Member member, YearMonth yearMonth) {
        int result = reportService.calculateMonthlyConsecutiveAchieve(reportService.getDailyAchievesForCurrentMonth(member, yearMonth));
        return result >= 15;
    }

    // 오뚝이 모양의 눈 조각
    public boolean isRolyPoly(Member member, YearMonth yearMonth) {
        LocalDate lastAchievedDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        List<DailyAchieveDto> dailyAchieves = statisticsService.getDailyAchieve(member, yearMonth.atDay(1).toString(), yearMonth.atEndOfMonth().toString());
        for (DailyAchieveDto dto : dailyAchieves) {
            // "ALL_ACHIEVED" 또는 "SOME_ACHIEVED" 상태면 진행한 날로 간주
            if ("ALL_ACHIEVED".equals(dto.getStatus()) || "SOME_ACHIEVED".equals(dto.getStatus())) {
                LocalDate currentDate = LocalDate.parse(dto.getDate(), formatter);
                if (lastAchievedDate != null) {
                    // 두 달성일 사이의 간격 계산
                    long gapDays = ChronoUnit.DAYS.between(lastAchievedDate, currentDate);
                    if (gapDays >= 14) {
                        // 14일 이상 건너뛰고 할 일이 진행된 경우 발견
                        return true;
                    }
                }
                lastAchievedDate = currentDate;
            }
        }
        return false;
    }

    // 비커 모양의 눈 조각
    public boolean isBeaker(Member member, YearMonth yearMonth) {
        // 저번 달에 새로운 카테고리의 할 일을 생성한 경우
        List<Category> categoryList = categoryRepository.findByMemberAndCreatedAtBetween(member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
        return !categoryList.isEmpty();
    }

    // 천사 날개의 눈 조각
    public boolean isAngel(Member member, YearMonth yearMonth) {
        List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetWeen(member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
        Set<String> categories = todoLogs.stream()
                .map(todoLog -> todoLog.getTodo().getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return categories.size() == 1;
    }

    // 한 입 베어먹은 푸딩의 눈 조각
    public boolean ispudding(Member member, YearMonth yearMonth) {
        // 지난 달 투두 로그 불러와서 하나의 투두만 있는지 확인
        List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetWeen(member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
        Set<Todo> todos = todoLogs.stream()
                .map(TodoLog::getTodo)
                .collect(Collectors.toSet());
        return todos.size() == 1;
    }

    // 요정 모양의 눈 조각
    public boolean isFairy(Member member, YearMonth yearMonth) {
        // 지난 달에 AI 루틴만 진행한 경우
        List<RoutineLog> routineLogs = routineLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
        return routineLogs.stream()
                .map(RoutineLog::getRoutine)
                .allMatch(Routine::getIsAiRoutine);
    }

    // 소라게 모양의 눈 조각
    public boolean isCrab(Member member, YearMonth yearMonth) {
        // 지난 달에 자율 루틴만 진행한 경우
        List<RoutineLog> routineLogs = routineLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
        return routineLogs.stream()
                .map(RoutineLog::getRoutine)
                .noneMatch(Routine::getIsAiRoutine);
    }

    // 스프링 모양의 눈 조각
    public boolean isSpring(Member member) {
        // 하나의 카테고리를 500시간 이상 진행한 경우(누적)
        List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompleted(member);
        Map<String, Long> categoryDuration = todoLogs.stream()
                .collect(Collectors.groupingBy(todoLog -> todoLog.getTodo().getCategory(), Collectors.summingLong(TodoLog::getDuration)));
        return categoryDuration.values().stream().anyMatch(duration -> duration >= 30000);
    }
}


