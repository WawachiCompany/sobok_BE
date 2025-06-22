package com.chihuahua.sobok.member.point;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PremiumExpirationScheduler {
    private final PremiumRepository premiumRepository;
    private final MemberRepository memberRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void checkAndExpirePremiums() {
        LocalDate today = LocalDate.now();
        List<Premium> premiums = premiumRepository.findAll();
        for (Premium premium : premiums) {
            if (premium.getEndAt().isBefore(today)) {
                Member member = premium.getMember();
                member.setIsPremium(false);
                memberRepository.save(member);
            }
        }
        // 스케줄러 성공 시 heartbeat URL로 GET 요청
        String heartbeatUrl = "https://uptime.betterstack.com/api/v1/heartbeat/zA6adsW6iGdLfuRS3xtR9qE1";
        try {
            restTemplate.getForObject(heartbeatUrl, String.class);
        } catch (Exception e) {
            System.err.println("Heartbeat 전송 실패: " + e.getMessage());
        }
    }
}
