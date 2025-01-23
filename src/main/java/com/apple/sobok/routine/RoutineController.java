package com.apple.sobok.routine;


import com.apple.sobok.account.Account;
import com.apple.sobok.account.AccountRepository;
import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.routine.todo.Todo;
import com.apple.sobok.routine.todo.TodoDto;
import com.apple.sobok.routine.todo.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineRepository routineRepository;
    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final TodoRepository todoRepository;


    @PostMapping("/routine/create")
    public ResponseEntity<?> createRoutine(@RequestBody RoutineDto routineDto) {
        Routine routine = new Routine();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Account account = accountRepository.findById(
                routineDto.getAccountId()).orElseThrow(
                        () -> new IllegalArgumentException("해당 적금이 존재하지 않습니다."));
        routine.setMember(member);
        routine.setAccount(account);
        routine.setTitle(routineDto.getTitle());
        routine.setStartTime(routineDto.getStartTime());
        routine.setEndTime(routineDto.getEndTime());
        routine.setDuration(Duration.between(routineDto.getStartTime(), routineDto.getEndTime()).toMinutes());
        routine.setDays(routineDto.getDays());
        routine.setCreatedAt(LocalDateTime.now());
        routine.setIsSuspended(false);
        routine.setIsCompleted(false);
        routine.setIsEnded(false);
        routineRepository.save(routine);

        // Todo 생성 로직 추가
        if (routineDto.getTodos() != null) {
            for (TodoDto todoDto : routineDto.getTodos()) {
                Todo todo = new Todo();
                todo.setRoutine(routine);
                todo.setTitle(todoDto.getTitle());
                todo.setStartTime(todoDto.getStartTime());
                todo.setEndTime(todoDto.getEndTime());
                todo.setDuration(Duration.between(todoDto.getStartTime(), todoDto.getEndTime()).toMinutes());
                todo.setLinkApp(todoDto.getLinkApp());
                todo.setIsCompleted(false);
                todoRepository.save(todo);
            }
        }



        Map<String, Object> response = Map.of(
                "message", "루틴이 생성되었습니다.",
                "timestamp", LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/routine/update")
    public ResponseEntity<?> updateRoutine(@RequestBody RoutineDto routineDto, @RequestParam Long routineId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        var result = routineRepository.findByMemberAndId(member, routineId);
        if (result.isPresent()) {
            Routine routine = result.get();
            Account account = accountRepository.findById(
                    routineDto.getAccountId()).orElseThrow(
                    () -> new IllegalArgumentException("해당 적금이 존재하지 않습니다."));
            routine.setAccount(account);
            routine.setTitle(routineDto.getTitle());
            routine.setStartTime(routineDto.getStartTime());
            routine.setEndTime(routineDto.getEndTime());
            routine.setDuration(Duration.between(routineDto.getStartTime(), routineDto.getEndTime()).toMinutes());
            routine.setDays(routineDto.getDays());
            routineRepository.save(routine);

            // Todo 업데이트 로직 추가
            List<Todo> todos = todoRepository.findByRoutine(routine);
            todoRepository.deleteAll(todos);
            if (routineDto.getTodos() != null) {
                for (TodoDto todoDto : routineDto.getTodos()) {
                    Todo todo = new Todo();
                    todo.setRoutine(routine);
                    todo.setTitle(todoDto.getTitle());
                    todo.setStartTime(todoDto.getStartTime());
                    todo.setEndTime(todoDto.getEndTime());
                    todo.setDuration(Duration.between(todoDto.getStartTime(), todoDto.getEndTime()).toMinutes());
                    todo.setLinkApp(todoDto.getLinkApp());
                    todo.setIsCompleted(false);
                    todoRepository.save(todo);
                }
            }

            return ResponseEntity.ok(Map.of("message", "루틴이 업데이트되었습니다."));
        }
        return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));
    }

    @DeleteMapping("/routine/delete")
    public ResponseEntity<?> deleteRoutine(@RequestParam Long routineId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        var result = routineRepository.findByMemberAndId(member, routineId);
        if (result.isPresent()) {
            routineRepository.delete(result.get());
            return ResponseEntity.ok(Map.of("message", "루틴이 삭제되었습니다."));
        }
        return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));
    }

    @GetMapping("/routine/by-date")
    public ResponseEntity<?> getTodayRoutine(@RequestParam String dateString) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        String dayOfWeek = date.getDayOfWeek().toString();
        List<Routine> result = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "오늘의 루틴이 없습니다."));
        }
        List<Map<String, Object>> routines = result.stream()
                .map(this::convertToMapCal)
                .toList();

        return ResponseEntity.ok(routines);
    }

    @GetMapping("/routine/by-list")
    public ResponseEntity<?> getAllRoutine() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        List<Routine> result = routineRepository.findByMember(member);
        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "루틴이 없습니다."));
        }
        List<Map<String, Object>> routines = result.stream()
                .map(this::convertToMapList)
                .toList();

        return ResponseEntity.ok(routines);
    }

    @GetMapping("/routine/detail")
    public ResponseEntity<?> getRoutine(@RequestParam Long routineId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        var result = routineRepository.findByMemberAndId(member, routineId);
        if (result.isPresent()) {
            return ResponseEntity.ok(convertToMap(result.get()));
        }
        return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));

    }

    private Map<String, Object> convertToMap(Routine routine) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", routine.getId());
        response.put("accountTitle", routine.getAccount().getTitle());
        response.put("title", routine.getTitle());
        response.put("days", routine.getDays());
        response.put("startTime", routine.getStartTime());
        response.put("endTime", routine.getEndTime());
        response.put("duration", routine.getDuration());
        response.put("isSuspended", routine.getIsSuspended());
        response.put("isCompleted", routine.getIsCompleted());
        response.put("isEnded", routine.getIsEnded());
        response.put("todos", routine.getTodos().stream()
                        .map(todo -> Map.of(
                                "title", todo.getTitle(),
                                "startTime", todo.getStartTime(),
                                "endTime", todo.getEndTime(),
                                "duration", todo.getDuration(),
                                "linkApp", todo.getLinkApp(),
                                "isCompleted", todo.getIsCompleted()
                        ))
                        .toList());
        return response;
    }

    private Map<String, Object> convertToMapCal(Routine routine) {
        return Map.of(
                "title", routine.getTitle(),
                "accountTitle", routine.getAccount().getTitle(),
                "startTime", routine.getStartTime(),
                "endTime", routine.getEndTime(),
                "duration", routine.getDuration()
        );
    }

    private Map<String, Object> convertToMapList(Routine routine) {
        return Map.of(
                "title", routine.getTitle(),
                "accountTitle", routine.getAccount().getTitle(),
                "duration", routine.getDuration(),
                "isSuspended", routine.getIsSuspended()
        );
    }

    @GetMapping("/routine/today/not-completed")
    private ResponseEntity<?> getTodayWillRoutine() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        LocalDate date = LocalDate.now();
        String dayOfWeek = date.getDayOfWeek().toString();
        List<Routine> routines = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
        if (routines.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "오늘 완료하지 않은 루틴이 없습니다."));
        }
        List<Map<String, Object>> result = routines.stream()
                .filter(routine -> !routine.getIsCompleted())
                .map(this::convertToMapList)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/routine/today/completed")
    private ResponseEntity<?> getTodayDoneRoutine() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        LocalDate date = LocalDate.now();
        String dayOfWeek = date.getDayOfWeek().toString();
        List<Routine> routines = routineRepository.findByUserIdAndDayCompleted(member.getId(), dayOfWeek);
        if (routines.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "오늘 완료한 루틴이 없습니다."));
        }
        List<Map<String, Object>> result = routines.stream()
                .map(this::convertToMapList)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/routine/is-ended")
    private ResponseEntity<?> getIsThereEndedRoutine() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        List<Routine> routines = routineRepository.findByMemberAndIsEnded(member, true);
        if (routines.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "만료된 루틴이 없습니다."));
        }
        List<Map<String, Object>> result = routines.stream()
                .map(this::convertToMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/routine/suspend")
    private ResponseEntity<?> suspendRoutine(@RequestParam Long routineId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        var result = routineRepository.findByMemberAndId(member, routineId);
        if (result.isPresent()) {
            Routine routine = result.get();
            if(!routine.getIsSuspended()) {
                routine.setIsSuspended(true);
                routineRepository.save(routine);
                return ResponseEntity.ok(Map.of("message", "루틴이 중단되었습니다."));
            }
            else{
                routine.setIsSuspended(false);
                routineRepository.save(routine);
                return ResponseEntity.ok(Map.of("message", "루틴이 재개되었습니다."));
            }

        }
        return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));
    }
}
