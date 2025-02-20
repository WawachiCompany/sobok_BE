package com.apple.sobok.routine;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.statistics.DailyAchieve;
import com.apple.sobok.statistics.DailyAchieveRepository;
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
    private final DailyAchieveRepository dailyAchieveRepository;


    // 연속 달성일 계산
    @Scheduled(cron = "0 5 0 * * ?") // 매일 00:05에 실행
    public void checkAndExpireRoutines() {
        List<Member> members = memberRepository.findAll();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DayOfWeek yesterdayDayOfWeek = yesterday.getDayOfWeek();
        String targetDay = yesterdayDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
        for(Member member: members){

            // 일별 달성 상태 저장
            DailyAchieve dailyAchieve = new DailyAchieve();
            dailyAchieve.setMember(member);
            dailyAchieve.setDate(yesterday);

            List<Routine> todayRoutines = member.getRoutines().stream()
                    .filter(routine -> routine.getDays().contains(targetDay))
                    .toList();

            if(todayRoutines.isEmpty()){
                dailyAchieve.setStatus("NO_ROUTINE");
                dailyAchieveRepository.save(dailyAchieve);
                continue;
            }

            boolean isCompleted = todayRoutines
                    .stream()
                    .anyMatch(Routine::getIsAchieved); // 완료한 루틴 하나라도 있으면 연속달성일 인정
            boolean isAllAchieved = todayRoutines
                    .stream()
                    .allMatch(Routine::getIsAchieved); // 모든 루틴을 완료했는지 확인
            if (isCompleted) {
                member.setConsecutiveAchieveCount(member.getConsecutiveAchieveCount() + 1);
                if(isAllAchieved){
                    dailyAchieve.setStatus("ALL_ACHIEVED");
                } else {
                    dailyAchieve.setStatus("SOME_ACHIEVED");
                }

            } else {
                member.setConsecutiveAchieveCount(0);
                dailyAchieve.setStatus("NONE_ACHIEVED");
            }
            member.getRoutines().forEach(routine -> routine.setIsAchieved(false));
            memberRepository.save(member);
            dailyAchieveRepository.save(dailyAchieve);
        }
    }
}
