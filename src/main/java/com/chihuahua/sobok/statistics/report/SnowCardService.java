package com.chihuahua.sobok.statistics.report;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineLog;
import com.chihuahua.sobok.routine.RoutineLogRepository;
import com.chihuahua.sobok.routine.todo.Category;
import com.chihuahua.sobok.routine.todo.CategoryRepository;
import com.chihuahua.sobok.routine.todo.Todo;
import com.chihuahua.sobok.routine.todo.TodoLog;
import com.chihuahua.sobok.routine.todo.TodoLogRepository;
import com.chihuahua.sobok.routine.todo.TodoRepository;
import com.chihuahua.sobok.statistics.DailyAchieveDto;
import com.chihuahua.sobok.statistics.StatisticsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnowCardService {

  private final TodoRepository todoRepository;
  private final TodoLogRepository todoLogRepository;
  private final StatisticsService statisticsService;
  private final ReportService reportService;
  private final CategoryRepository categoryRepository;
  private final RoutineLogRepository routineLogRepository;
  private final SnowCardRepository snowCardRepository;

  // 눈 카드 달성 조건 확인 후 뽑기
  public Map<String, String> getSnowCard(Member member, String yearMonth) {
    try {
      YearMonth yearMonthObj = YearMonth.parse(yearMonth);
      Optional<SnowCard> snowCardOptional = snowCardRepository.findByMemberIdAndTargetYearMonth(
          member.getId(), yearMonth);
      if (snowCardOptional.isPresent()) {
        Map<String, String> result = new HashMap<>();
        result.put("snowCard", snowCardOptional.get().getSnowCard());
        return result;
      }

      List<String> snowCards = new ArrayList<>();

      // 각 조건 체크 메서드를 안전하게 실행
      tryAddSnowCard(snowCards, "hexagon", () -> isHexagon(member));

      // 카테고리 관련 카드는 null 반환 가능성이 있으므로 별도 처리
      try {
        String categoryCard = getMostPerformedCategoryLastMonth(member, yearMonthObj);
        if (categoryCard != null && !categoryCard.equals("none")) {
          snowCards.add(categoryCard);
        }
      } catch (Exception e) {
        log.error("카테고리 카드 조회 중 오류 발생: {}", e.getMessage(), e);
      }

      // 달 카드도 null 반환 가능성이 있으므로 별도 처리
      try {
        String moonCard = getMoon(member, yearMonthObj);
        if (moonCard != null) {
          snowCards.add(moonCard);
        }
      } catch (Exception e) {
        log.error("달 카드 조회 중 오류 발생: {}", e.getMessage(), e);
      }

      tryAddSnowCard(snowCards, "like", () -> isLike(member, yearMonthObj));
      tryAddSnowCard(snowCards, "rolyPoly", () -> isRolyPoly(member, yearMonthObj));
      tryAddSnowCard(snowCards, "beaker", () -> isBeaker(member, yearMonthObj));
      tryAddSnowCard(snowCards, "angel", () -> isAngel(member, yearMonthObj));
      tryAddSnowCard(snowCards, "pudding", () -> isPudding(member, yearMonthObj));
      tryAddSnowCard(snowCards, "fairy", () -> isFairy(member, yearMonthObj));
      tryAddSnowCard(snowCards, "crab", () -> isCrab(member, yearMonthObj));
      tryAddSnowCard(snowCards, "spring", () -> isSpring(member));
      tryAddSnowCard(snowCards, "donut", () -> isDonut(member));

      // 뱀 모양의 눈 조각(2025년 한정)
      if (yearMonthObj.getYear() == 2025) {
        snowCards.add("snake");
      }

      // 만약 모든 카드 검사에 실패하여 리스트가 비어있다면 기본 카드 추가
      if (snowCards.isEmpty()) {
        log.warn("Member ID: {}의 달성 가능한 눈 카드가 없습니다. 기본 카드를 추가합니다.", member.getId());
        snowCards.add("basic");
      }

      Map<String, String> result = new HashMap<>();
      Random random = new Random();
      String card = snowCards.get(random.nextInt(snowCards.size()));
      result.put("snowCard", card);

      // 눈 카드 뽑은 후 저장
      try {
        SnowCard snowCard = new SnowCard();
        snowCard.setSnowCard(card);
        snowCard.setMember(member);
        snowCard.setTargetYearMonth(yearMonth);
        snowCardRepository.save(snowCard);
      } catch (Exception e) {
        log.error("Member ID: {}의 눈 카드 저장 중 오류 발생: {}", member.getId(), e.getMessage(), e);
        // 저장 실패해도 결과는 반환
      }

      return result;
    } catch (Exception e) {
      log.error("눈 카드 생성 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
      Map<String, String> errorResult = new HashMap<>();
      errorResult.put("snowCard", "basic");
      errorResult.put("error", "눈 카드 생성 중 오류가 발생했습니다.");
      return errorResult;
    }
  }

  private void tryAddSnowCard(List<String> snowCards, String cardName,
      Supplier<Boolean> conditionCheck) {
    try {
      if (conditionCheck.get()) {
        snowCards.add(cardName);
      }
    } catch (Exception e) {
      log.error("{} 카드 검사 중 오류 발생: {}", cardName, e.getMessage(), e);
    }
  }


  // 육각형 모양의 눈 조각
  public boolean isHexagon(Member member) {
    try {
      List<Todo> todoList = todoRepository.findAllByMember(member);
      Set<String> categories = todoList.stream()
          .map(Todo::getCategory)
          .filter(Objects::nonNull)  // null 카테고리 필터링
          .collect(Collectors.toSet());
      return categories.size() > 6;
    } catch (Exception e) {
      log.error("Member ID: {}의 육각형 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // tryAddSnowCard에서 처리됨
    }
  }


  // 카테고리 가장 많이 한거
  public String getMostPerformedCategoryLastMonth(Member member, YearMonth yearMonth) {
    try {
      LocalDateTime startOfLastMonth = yearMonth.atDay(1).atStartOfDay();
      LocalDateTime endOfLastMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

      List<TodoLog> lastMonthTodoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
          member, startOfLastMonth, endOfLastMonth);
      Map<String, Long> categoryCount = lastMonthTodoLogs.stream()
          .map(todoLog -> todoRepository.findById(todoLog.getTodo().getId()).orElse(null))
          .filter(Objects::nonNull)
          .collect(Collectors.groupingBy(Todo::getCategory, Collectors.counting()));

      return categoryCount.entrySet().stream()
          .max(Map.Entry.comparingByValue())
          .map(Map.Entry::getKey)
          .orElse("none");
    } catch (Exception e) {
      log.error("Member ID: {}의 카테고리 카드 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨

    }
  }

  // 달 모양의 눈 조각
  public String getMoon(Member member, YearMonth yearMonth) {
    try {
      List<DailyAchieveDto> result = statisticsService.getDailyAchieve(member,
          yearMonth.atDay(1).toString(), yearMonth.atEndOfMonth().toString());
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
    } catch (Exception e) {
      log.error("Member ID: {}의 달 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }


  // 좋아하는 마음의 눈 조각
  public boolean isLike(Member member, YearMonth yearMonth) {
    try {
      int result = reportService.calculateMonthlyConsecutiveAchieve(
          reportService.getDailyAchievesForCurrentMonth(member, yearMonth));
      return result >= 15;
    } catch (Exception e) {
      log.error("Member ID: {}의 좋아하는 마음의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 오뚝이 모양의 눈 조각
  public boolean isRolyPoly(Member member, YearMonth yearMonth) {
    try {
      LocalDate lastAchievedDate = null;
      DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

      List<DailyAchieveDto> dailyAchieves = statisticsService.getDailyAchieve(member,
          yearMonth.atDay(1).toString(), yearMonth.atEndOfMonth().toString());
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
    } catch (Exception e) {
      log.error("Member ID: {}의 오뚝이 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 비커 모양의 눈 조각
  public boolean isBeaker(Member member, YearMonth yearMonth) {
    try {
      // 저번 달에 새로운 카테고리의 할 일을 생성한 경우
      List<Category> categoryList = categoryRepository.findByMemberAndCreatedAtBetween(member,
          yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
      return !categoryList.isEmpty();
    } catch (Exception e) {
      log.error("Member ID: {}의 비커 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 천사 날개의 눈 조각
  public boolean isAngel(Member member, YearMonth yearMonth) {
    try {
      List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
          member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
      if (todoLogs.isEmpty()) {
        return false; // 로그가 없으면 조건을 만족할 수 없음
      }

      Set<String> categories = todoLogs.stream()
          .map(todoLog -> todoLog.getTodo().getCategory())
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
      return categories.size() == 1;
    } catch (Exception e) {
      log.error("Member ID: {}의 천사 날개의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 한 입 베어먹은 푸딩의 눈 조각
  public boolean isPudding(Member member, YearMonth yearMonth) {
    try {
      // 지난 달 투두 로그 불러와서 하나의 투두만 있는지 확인
      List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
          member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
      if (todoLogs.isEmpty()) {
        return false; // 로그가 없으면 조건을 만족할 수 없음
      }

      Set<Todo> todos = todoLogs.stream()
          .map(TodoLog::getTodo)
          .collect(Collectors.toSet());
      return todos.size() == 1;
    } catch (Exception e) {
      log.error("Member ID: {}의 푸딩 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 요정 모양의 눈 조각
  public boolean isFairy(Member member, YearMonth yearMonth) {
    try {
      // 지난 달에 AI 루틴만 진행한 경우
      List<RoutineLog> routineLogs = routineLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
          member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
      if (routineLogs.isEmpty()) {
        return false; // 로그가 없으면 조건을 만족할 수 없음
      }
      return routineLogs.stream()
          .map(RoutineLog::getRoutine)
          .allMatch(Routine::getIsAiRoutine);
    } catch (Exception e) {
      log.error("Member ID: {}의 요정 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 소라게 모양의 눈 조각
  public boolean isCrab(Member member, YearMonth yearMonth) {
    try {
      // 지난 달에 자율 루틴만 진행한 경우
      List<RoutineLog> routineLogs = routineLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
          member, yearMonth.atDay(1).atStartOfDay(), yearMonth.atEndOfMonth().atTime(23, 59, 59));
      if (routineLogs.isEmpty()) {
        return false; // 로그가 없으면 조건을 만족할 수 없음
      }

      return routineLogs.stream()
          .map(RoutineLog::getRoutine)
          .noneMatch(Routine::getIsAiRoutine);
    } catch (Exception e) {
      log.error("Member ID: {}의 소라게 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 스프링 모양의 눈 조각
  public boolean isSpring(Member member) {
    try {
      // 하나의 카테고리를 500시간 이상 진행한 경우(누적)
      List<TodoLog> todoLogs = todoLogRepository.findAllByMemberAndIsCompleted(member);
      if (todoLogs.isEmpty()) {
        return false; // 로그가 없으면 조건을 만족할 수 없음
      }

      Map<String, Long> categoryDuration = todoLogs.stream()
          .collect(Collectors.groupingBy(todoLog -> todoLog.getTodo().getCategory(),
              Collectors.summingLong(TodoLog::getDuration)));
      return categoryDuration.values().stream().anyMatch(duration -> duration >= 30000);
    } catch (Exception e) {
      log.error("Member ID: {}의 스프링 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  // 도넛 모양의 눈 조각
  public boolean isDonut(Member member) {
    try {
      YearMonth lastMonth = YearMonth.now().minusMonths(1);
      LocalDate startOfLastMonth = lastMonth.atDay(1);
      LocalDate endOfLastMonth = lastMonth.atEndOfMonth();

      return member.getAccounts().stream()
          .anyMatch(account -> account.getExpiredAt().isAfter(startOfLastMonth.minusDays(1)) &&
              account.getExpiredAt().isBefore(endOfLastMonth.plusDays(1)));
    } catch (Exception e) {
      log.error("Member ID: {}의 도넛 모양의 눈 조각 검사 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      throw e;  // getSnowCard에서 직접 처리됨
    }
  }

  public List<SnowCardResponseDto> getAllSnowCard(Member member) {
    try {
      List<SnowCard> snowCardList = snowCardRepository.findAllByMemberId(member.getId());
      return snowCardList.stream().map(snowCard -> {
        SnowCardResponseDto dto = new SnowCardResponseDto();
        dto.setYearMonth(snowCard.getTargetYearMonth());
        dto.setSnowCard(snowCard.getSnowCard());
        return dto;
      }).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Member ID: {}의 모든 눈 카드 조회 중 오류 발생: {}", member.getId(), e.getMessage(), e);
      return new ArrayList<>();  // 오류 발생 시 빈 리스트 반환
    }

  }
}


