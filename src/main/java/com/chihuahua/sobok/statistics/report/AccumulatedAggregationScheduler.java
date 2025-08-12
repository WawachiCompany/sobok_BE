package com.chihuahua.sobok.statistics.report;

import com.chihuahua.sobok.account.AccountLog;
import com.chihuahua.sobok.account.AccountLogRepository;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Profile("prod")
@Slf4j
public class AccumulatedAggregationScheduler {

  private final MemberRepository memberRepository;
  private final AccountLogRepository accountLogRepository;
  private final RoutineRepository routineRepository;
  private final MonthlyUserReportRepository monthlyUserReportRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  @Scheduled(cron = "0 0 0 L * *")
  @Transactional
  public void aggregationMonthlyUserAccumulation() {
    try {
      log.info("월별 사용자 통계 집계 시작");
      YearMonth currentYearMonth = YearMonth.now();
      String month = currentYearMonth.toString();
      LocalDateTime startOfMonth = currentYearMonth.atDay(1).atStartOfDay();
      LocalDateTime endOfMonth = currentYearMonth.atEndOfMonth().plusDays(1).atStartOfDay()
          .minusNanos(1);

      List<Member> members = memberRepository.findAll();
      int totalMembers = members.size();
      int processedMembers = 0;

      for (Member member : members) {
        try {
          processMemberAggregation(member, month, startOfMonth, endOfMonth);
          processedMembers++;
          if (processedMembers % 100 == 0) { // 100명마다 진행상황 로깅
            log.info("사용자 통계 처리 진행 중: {}/{}", processedMembers, totalMembers);
          }
        } catch (Exception e) {
          log.error("멤버 ID: {}의 월별 통계 집계 중 오류 발생: {}", member.getId(), e.getMessage(), e);
          // 단일 멤버 오류 시 다른 멤버는 계속 처리
        }
      }
      log.info("월별 사용자 통계 집계 완료: 총 {}명 처리됨", processedMembers);
      // 스케줄러 성공 시 heartbeat URL로 GET 요청
      String heartbeatUrl = "https://uptime.betterstack.com/api/v1/heartbeat/khFqeznCQdFsCVZeyr6bz9Yk";
      try {
        restTemplate.getForObject(heartbeatUrl, String.class);
      } catch (Exception e) {
        log.error("Heartbeat 전송 실패: {}", e.getMessage(), e);
      }
    } catch (Exception e) {
      log.error("월별 사용자 통계 집계 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
      throw e; // 상위 호출자에게 예외 전파
    }
  }

  private void processMemberAggregation(Member member, String month, LocalDateTime startOfMonth,
      LocalDateTime endOfMonth) {
    // 이미 해당 월에 대한 보고서가 있는지 확인
    Optional<MonthlyUserReport> existingReport = monthlyUserReportRepository
        .findByMemberIdAndTargetYearMonth(member.getId(), month);

    if (existingReport.isPresent()) {
      log.warn("멤버 ID: {}의 {} 월 보고서가 이미 존재합니다. 기존 보고서 갱신.", member.getId(), month);
      // 기존 보고서 삭제 또는 업데이트 로직이 필요하다면 여기에 추가
      monthlyUserReportRepository.delete(existingReport.get());
    }

    long totalDepositTime = calculateTotalDepositTime(member, startOfMonth, endOfMonth);

    int activeDays = countDaysInMonth(getMemberDays(member), month);
    long averageAccumulatedTime = activeDays > 0 ? totalDepositTime / activeDays : 0;

    MonthlyUserReport monthlyUserReport = new MonthlyUserReport();
    monthlyUserReport.setMember(member);
    monthlyUserReport.setTargetYearMonth(month);
    monthlyUserReport.setTotalAccumulatedTime(totalDepositTime);
    monthlyUserReport.setAverageAccumulatedTime(averageAccumulatedTime);

    monthlyUserReportRepository.save(monthlyUserReport);
  }

  private long calculateTotalDepositTime(Member member, LocalDateTime startOfMonth,
      LocalDateTime endOfMonth) {
    try {
      return member.getAccounts().stream()
          .flatMap(account -> {
            try {
              return accountLogRepository.findByAccountAndCreatedAtBetween(
                  account, startOfMonth, endOfMonth).stream();
            } catch (Exception e) {
              log.error("적금 ID: {}의 로그 조회 중 오류 발생: {}", account.getId(), e.getMessage());
              return Stream.empty();
            }
          })
          .mapToLong(AccountLog::getDepositTime)
          .sum();
    } catch (Exception e) {
      log.error("총 적립 시간 계산 중 오류 발생: {}", e.getMessage());
      return 0; // 오류 발생 시 기본값 반환
    }
  }

  // 각 멤버가 가진 루틴 시행 요일
  public Set<String> getMemberDays(Member member) {
    List<Routine> routines = routineRepository.findByMemberAndAccountIsExpired(member, false);
    return routines.stream()
        .flatMap(routine -> routine.getDays().stream())
        .collect(Collectors.toSet());
  }

  // 해당 월의 총 시행일 수 계산
  public int countDaysInMonth(Set<String> days, String yearMonth) {
    YearMonth yearMonthObj = YearMonth.parse(yearMonth);
    int count = 0;
    // 1일부터 해당 달의 마지막 날까지 반복
    for (int day = 1; day <= yearMonthObj.lengthOfMonth(); day++) {
      LocalDate date = yearMonthObj.atDay(day);
      // 요일은 영어 대문자로 나오므로
      String dayName = date.getDayOfWeek().name();
      if (days.contains(dayName)) {
        count++;
      }
    }
    return count;
  }
}
