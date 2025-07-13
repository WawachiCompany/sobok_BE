package com.chihuahua.sobok.account;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.point.PointLog;
import com.chihuahua.sobok.member.point.PointLogService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Profile("prod")
@Slf4j
public class AccountPointScheduler {

  private final AccountRepository accountRepository;
  private final AccountLogRepository accountLogRepository;
  private final InterestLogRepository interestLogRepository;
  private final PointLogService pointLogService;

  @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정에 실행
  @Transactional
  public void rewardPrincipalPoint() { // 원금 포인트 지급
    try {
      log.info("월별 원금 포인트 지급 스케줄러 시작");
      LocalDate today = LocalDate.now();
      List<Account> accounts = accountRepository.findByIsExpired(false);
      for (Account account : accounts) {
        Integer lastMonthTotalTime = accountLogRepository.findByAccount(account).stream()
            .filter(accountLog -> accountLog.getCreatedAt().getMonth()
                .equals(today.minusMonths(1).getMonth()))
            .mapToInt(AccountLog::getDepositTime).sum();
        if (lastMonthTotalTime == 0) {
          continue;
        }

        // 이전 달에 목표 달성한 비율 곱해서 이자 지급
        int interest = Math.round(
            lastMonthTotalTime * account.getInterest() / 100 * ((float) lastMonthTotalTime
                / account.getTime()));

        account.setInterestBalance(
            account.getInterestBalance() + interest); // 이자 포인트 반올림해서 적립
        accountRepository.save(account);

        InterestLog interestLog = new InterestLog();
        interestLog.setTargetYearMonth(
            today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        interestLog.setAccountId(account.getId());
        interestLog.setInterest(interest);
        interestLogRepository.save(interestLog);

        Member member = account.getMember();
        member.setPoint(member.getPoint() + lastMonthTotalTime);
        PointLog pointLog = new PointLog();
        pointLog.setMember(member);
        pointLog.setPoint(lastMonthTotalTime);
        pointLog.setBalance(member.getPoint());
        pointLog.setCategory("원금 포인트 적립");
        pointLogService.save(pointLog);
      }

      // 스케줄러 완료 후 BetterStack heartbeat 전송
      sendHeartbeat();
      log.info("월별 원금 포인트 지급 스케줄러 완료");
    } catch (Exception e) {
      // 에러 발생 시 로그 출력
      log.error("스케줄러 실행 중 오류 발생", e);
    }
  }

  private void sendHeartbeat() {
    try (java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient()) {
      java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
          .uri(java.net.URI.create(
              "https://uptime.betterstack.com/api/v1/heartbeat/ZAx7AYjTUi5RweptjcVSzoym"))
          .GET()
          .build();

      java.net.http.HttpResponse<String> response = client.send(request,
          java.net.http.HttpResponse.BodyHandlers.ofString());

      log.info("Heartbeat 전송 완료: {}", response.statusCode());
    } catch (Exception e) {
      log.error("Heartbeat 전송 실패", e);
    }
  }
}
