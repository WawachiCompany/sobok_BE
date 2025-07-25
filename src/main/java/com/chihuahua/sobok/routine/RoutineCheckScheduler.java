package com.chihuahua.sobok.routine;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.statistics.DailyAchieve;
import com.chihuahua.sobok.statistics.DailyAchieveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class RoutineCheckScheduler {

    private final MemberRepository memberRepository;
    private final DailyAchieveRepository dailyAchieveRepository;
    private final RoutineRepository routineRepository;
    private final RestTemplate restTemplate = new RestTemplate();


    // 연속 달성일 계산
    @Scheduled(cron = "0 5 0 * * ?") // 매일 00:05에 실행
    @Transactional
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

            boolean isAnyAchieved = todayRoutines
                    .stream()
                    .anyMatch(Routine::getIsAchieved); // 완료한 루틴 하나라도 있으면 연속달성일 인정
            boolean isAllCompleted = todayRoutines
                    .stream()
                    .allMatch(Routine::getIsCompleted); // 모든 루틴을 완료했는지 확인
            if (isAnyAchieved) {
                member.setConsecutiveAchieveCount(member.getConsecutiveAchieveCount() + 1);
                if(isAllCompleted){
                    dailyAchieve.setStatus("ALL_ACHIEVED");
                } else {
                    dailyAchieve.setStatus("SOME_ACHIEVED");
                }

            } else {
                member.setConsecutiveAchieveCount(0);
                dailyAchieve.setStatus("NONE_ACHIEVED");
            }

            // 루틴 isAchieved, isCompleted 상태 업데이트
            routineRepository.resetAchievedStatusByMemberId(member.getId());

            memberRepository.save(member);
            dailyAchieveRepository.save(dailyAchieve);
        }
        // 스케줄러 성공 시 heartbeat URL로 GET 요청
        String heartbeatUrl = "https://uptime.betterstack.com/api/v1/heartbeat/bxoKvzw1MonSEQoyaq1zrch2";
        try {
            restTemplate.getForObject(heartbeatUrl, String.class);
        } catch (Exception e) {
            // 예외 발생 시 로깅 등 필요시 처리
            System.err.println("Heartbeat 전송 실패: " + e.getMessage());
        }
    }
}
