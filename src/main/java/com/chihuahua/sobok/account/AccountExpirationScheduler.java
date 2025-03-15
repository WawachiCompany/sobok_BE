package com.chihuahua.sobok.account;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class AccountExpirationScheduler {

    private final AccountRepository accountRepository;

    @Scheduled(cron = "0 10 0 * * ?") // 매일 00:10에 실행
    public void checkAndExpireAccounts() {
        LocalDate today = LocalDate.now();
        List<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            if (account.getExpiredAt().isBefore(today) && !account.getIsExpired()) {
                account.setIsExpired(true);
                accountRepository.save(account);

            }
        }
    }
}
