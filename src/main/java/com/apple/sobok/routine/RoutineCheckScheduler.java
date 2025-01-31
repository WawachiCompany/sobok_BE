package com.apple.sobok.routine;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class RoutineCheckScheduler {

    private final MemberRepository memberRepository;



    @Scheduled(cron = "0 5 0 * * ?") // 매일 00:05에 실행
    public void checkAndExpireRoutines() {
        List<Member> members = memberRepository.findAll();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DayOfWeek yesterdayDayOfWeek = yesterday.getDayOfWeek();
        String targetDay = yesterdayDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
        for(Member member: members){
            boolean allCompleted = member.getRoutines().stream()
                    .filter(routine -> routine.getDays().contains(targetDay))
                    .allMatch(Routine::getIsCompleted);
            if (allCompleted) {
                member.setConsecutiveAchieveCount(member.getConsecutiveAchieveCount() + 1);
            } else {
                member.setConsecutiveAchieveCount(0);
            }
            member.getRoutines().forEach(routine -> routine.setIsCompleted(false));
            memberRepository.save(member);
        }
    }
}
