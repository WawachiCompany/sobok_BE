package com.apple.sobok.statistics;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final MemberService memberService;
    private final StatisticsService statisticsService;
    private final RoutineRepository routineRepository;

    @GetMapping("/date")
    public ResponseEntity<?> getDailyAchieve(@RequestBody DateRangeDto dateRangeDto) {
        Member member = memberService.getMember();
        return ResponseEntity.ok(statisticsService.getDailyAchieve(member, dateRangeDto.getStartDate(), dateRangeDto.getEndDate()));
    }

    @GetMapping("/routine")
    public ResponseEntity<?> getDailyRoutineAchieve(@RequestParam Long routineId, @RequestBody DateRangeDto dateRangeDto) {
        return ResponseEntity.ok(statisticsService.getDailyRoutineAchieve(routineId, dateRangeDto.getStartDate(), dateRangeDto.getEndDate()));
    }

    @GetMapping("/date/log")
    public ResponseEntity<?> getDailyAchieveLog(@RequestParam String date) {
        Member member = memberService.getMember();
        return ResponseEntity.ok(statisticsService.getDailyAchieveLog(member, date));
    }

    @GetMapping("/routine/log")
    public ResponseEntity<?> getDailyRoutineAchieveLog(@RequestParam Long routineId, @RequestParam String date) {
        return ResponseEntity.ok(statisticsService.getDailyRoutineAchieveLog(routineId, date));
    }
}
