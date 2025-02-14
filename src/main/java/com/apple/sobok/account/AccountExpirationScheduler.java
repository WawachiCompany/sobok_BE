package com.apple.sobok.account;


import com.apple.sobok.routine.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountExpirationScheduler {

    private final AccountRepository accountRepository;
    private final RoutineRepository routineRepository;

    @Scheduled(cron = "0 10 0 * * ?") // 매일 00:10에 실행
    public void checkAndExpireAccounts() {
        LocalDate today = LocalDate.now();
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            if (account.getExpiredAt().isBefore(today) && !account.getIsExpired()) {
                account.setIsExpired(true);
                accountRepository.save(account);

                // 계좌에 연결된 루틴도 모두 종료 처리
                account.getRoutines().forEach(routine -> {
                    routine.setIsEnded(true);
                    routine.setAccount(null);
                    routineRepository.save(routine);
                });
            }
        }
    }
}
