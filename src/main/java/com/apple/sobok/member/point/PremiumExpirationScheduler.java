package com.apple.sobok.member.point;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PremiumExpirationScheduler {
    private final PremiumRepository premiumRepository;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void checkAndExpirePremiums() {
        LocalDate today = LocalDate.now();
        List<Premium> premiums = premiumRepository.findAll();
        for (Premium premium : premiums) {
            if (premium.getEndAt().isBefore(today)) {
                premium.getMember().setIsPremium(false);
                premiumRepository.delete(premium);
            }
        }
    }
}
