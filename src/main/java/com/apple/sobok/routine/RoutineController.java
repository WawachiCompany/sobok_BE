package com.apple.sobok.routine;



import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.Map;


@Controller
@RequestMapping("/routine")
@RequiredArgsConstructor
public class RoutineController {
    private final MemberService memberService;
    private final RoutineService routineService;


    @PostMapping("/create")
    public ResponseEntity<?> createRoutine(@RequestBody RoutineDto routineDto) {
        Member member = memberService.getMember();

        routineService.createRoutine(routineDto, member);

        // 루틴 생성 후 주간 루틴 시간 계산
        routineService.calculateWeeklyRoutineTime(member);

        Map<String, Object> response = Map.of(
                "message", "루틴이 생성되었습니다.",
                "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateRoutine(@RequestBody RoutineDto routineDto, @RequestParam Long routineId) {
        Member member = memberService.getMember();
        routineService.updateRoutine(routineDto, member, routineId);

        // 루틴 수정 후 주간 루틴 시간 계산
        routineService.calculateWeeklyRoutineTime(member);
        return ResponseEntity.ok(Map.of("message", "루틴이 업데이트되었습니다."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRoutine(@RequestParam Long routineId) {
        Member member = memberService.getMember();
        routineService.deleteRoutine(member, routineId);

        // 루틴 삭제 후 주간 루틴 시간 계산
        routineService.calculateWeeklyRoutineTime(member);

        return ResponseEntity.ok(Map.of("message", "루틴이 삭제되었습니다."));

    }

    @GetMapping("/by-date")
    public ResponseEntity<?> getTodayRoutine(@RequestParam String dateString) {
        Member member = memberService.getMember();
        return routineService.getTodayRoutine(member, dateString);

    }

    @GetMapping("/by-list")
    public ResponseEntity<?> getAllRoutine() {
        Member member = memberService.getMember();

        return routineService.getAllRoutine(member);
    }

    @GetMapping("/detail")
    public ResponseEntity<?> getRoutine(@RequestParam Long routineId) {
//        Member member = memberService.getMember();
        return routineService.getRoutine(routineId);
    }


    @GetMapping("/today/not-completed")
    private ResponseEntity<?> getTodayWillRoutine() {
        Member member = memberService.getMember();
        return routineService.getTodayWillRoutine(member);
    }

    @GetMapping("/today/completed")
    private ResponseEntity<?> getTodayDoneRoutine() {
        Member member = memberService.getMember();
        return routineService.getTodayDoneRoutine(member);
    }

    @GetMapping("/is-ended")
    private ResponseEntity<?> getIsThereEndedRoutine() {
        Member member = memberService.getMember();
        return routineService.getIsThereEndedRoutine(member);
    }

    @GetMapping("/suspend")
    private ResponseEntity<?> suspendRoutine(@RequestParam Long routineId) {
        Member member = memberService.getMember();
        return routineService.suspendRoutine(member, routineId);
    }

    @PutMapping("/account")
    private ResponseEntity<?> connectAccount(@RequestParam Long routineId, @RequestParam Long accountId) {
        Member member = memberService.getMember();
        return routineService.connectAccount(member, routineId, accountId);
    }


}
