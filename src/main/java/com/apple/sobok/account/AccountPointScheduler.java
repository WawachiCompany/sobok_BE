package com.apple.sobok.account;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.point.PointLog;
import com.apple.sobok.member.point.PointLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class AccountPointScheduler {

    private final AccountRepository accountRepository;
    private final AccountLogRepository accountLogRepository;
    private final PointLogService pointLogService;

    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정에 실행
    public void rewardPrincipalPoint() { // 원금 포인트 지급
        LocalDate today = LocalDate.now();
        List<Account> accounts = accountRepository.findByIsExpired(false);
        for (Account account : accounts) {
            Integer lastMonthTotalTime = accountLogRepository.findByAccount(account).stream()
                    .filter(accountLog -> accountLog.getCreatedAt().getMonth().equals(today.minusMonths(1).getMonth()))
                    .mapToInt(AccountLog::getDepositTime).sum();
            if(lastMonthTotalTime == 0) {
                continue;
            }

            // 이전 달에 목표 달성한 비율 곱해서 이자 지급
            account.setInterestBalance(
                    account.getInterestBalance() + Math.round(
                            lastMonthTotalTime * account.getInterest() / 100 * ((float) lastMonthTotalTime / account.getTime()))); // 이자 포인트 반올림해서 적립
            accountRepository.save(account);

           Member member = account.getMember();
           member.setPoint(member.getPoint() + lastMonthTotalTime);
           PointLog pointLog = new PointLog();
           pointLog.setMember(member);
           pointLog.setPoint(lastMonthTotalTime);
           pointLog.setBalance(member.getPoint());
           pointLog.setCategory("원금 포인트 적립");
           pointLog.setCreatedAt(LocalDateTime.now());
           pointLogService.save(pointLog);
        }

        }
    }

