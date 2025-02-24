package com.apple.sobok.statistics;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.apple.sobok.statistics.StatisticsService;


@Controller
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {

    private final MemberService memberService;
    private final StatisticsService statisticsService;

    @GetMapping("/date")
    public ResponseEntity<?> getDailyAchieve(@RequestParam String startDate, @RequestParam String endDate) {
        Member member = memberService.getMember();
        return ResponseEntity.ok(statisticsService.getDailyAchieve(member, startDate, endDate));
    }

    @GetMapping("/routine")
    public ResponseEntity<?> getDailyRoutineAchieve(@RequestParam Long routineId, @RequestParam String startDate, @RequestParam String endDate) {
        return ResponseEntity.ok(statisticsService.getDailyRoutineAchieve(routineId, startDate, endDate));
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

    @GetMapping("/date/count")
    public ResponseEntity<?> getDateTimeStatistics(@RequestParam String startDate, @RequestParam String endDate) {
        Member member = memberService.getMember();
        return ResponseEntity.ok(statisticsService.getDateTimeStatistics(member, startDate, endDate));
    }

    @GetMapping("/routine/count")
    public ResponseEntity<?> getRoutineTimeStatistics(@RequestParam Long routineId) {
        return ResponseEntity.ok(statisticsService.getRoutineTimeStatistics(routineId));
    }
}
