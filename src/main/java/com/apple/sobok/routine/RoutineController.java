package com.apple.sobok.routine;


import com.apple.sobok.account.Account;
import com.apple.sobok.account.AccountRepository;
import com.apple.sobok.account.AccountService;
import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.member.MemberService;
import com.apple.sobok.routine.todo.Todo;
import com.apple.sobok.routine.todo.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/routine")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineRepository routineRepository;
    private final AccountRepository accountRepository;
    private final TodoRepository todoRepository;
    private final AccountService accountService;
    private final MemberService memberService;
    private final RoutineService routineService;


    @PostMapping("/create")
    public ResponseEntity<?> createRoutine(@RequestBody RoutineDto routineDto) {
        Member member = memberService.getMember();

        routineService.createRoutine(routineDto, member);

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
        return ResponseEntity.ok(Map.of("message", "루틴이 업데이트되었습니다."));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteRoutine(@RequestParam Long routineId) {
        Member member = memberService.getMember();
        routineService.deleteRoutine(member, routineId);

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
        Member member = memberService.getMember();
        return routineService.getRoutine(member, routineId);
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


}
