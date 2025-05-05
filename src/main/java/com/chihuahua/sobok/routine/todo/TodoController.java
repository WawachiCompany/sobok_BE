package com.chihuahua.sobok.routine.todo;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequiredArgsConstructor
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;
    private final MemberService memberService;

    @PostMapping("/start")
    public ResponseEntity<?> startTodo(@RequestParam Long todoId) {
        return todoService.startTodo(todoId);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endTodo(@RequestParam Long todoLogId, @RequestParam Long duration) {
        return todoService.endTodo(todoLogId, duration);
    }

    @GetMapping("/category")
    public ResponseEntity<?> getTodoCategory() {
        return todoService.getTodoCategory();
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayTodos() {
        return todoService.getTodayTodos();
    }

    @GetMapping("/closest")
    public ResponseEntity<?> getClosestTodo() {
        Member member = memberService.getMember();
        return todoService.getClosestTodo(member);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTodos() { return todoService.getAllTodos(); }

    @PutMapping("/update")
    public ResponseEntity<?> updateTodo(@RequestBody TodoDto todoDto) {
        return todoService.updateTodo(todoDto);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteTodo(@RequestParam Long todoId) {
        return todoService.deleteTodo(todoId);
    }

    @GetMapping("/overlap")
    public ResponseEntity<?> checkOverlap(
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam List<String> days  // String 형식으로 변경 (예: "MON", "TUE", "WED")
    ) {
        Member member = memberService.getMember();
        OverlapTimeCheckDto overlapTimeCheckDto = new OverlapTimeCheckDto();
        overlapTimeCheckDto.setStartTime(startTime);
        overlapTimeCheckDto.setEndTime(endTime);
        overlapTimeCheckDto.setDays(days);
        boolean isOverlaped = todoService.checkOverlap(member, overlapTimeCheckDto);

        return ResponseEntity.ok(Map.of("isOverlaped" , isOverlaped));
    }
}
