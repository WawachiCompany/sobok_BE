package com.apple.sobok.routine.todo;



import com.apple.sobok.account.AccountService;
import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineLog;
import com.apple.sobok.routine.RoutineLogRepository;
import com.apple.sobok.routine.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoLogRepository todoLogRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineRepository routineRepository;
    private final AccountService accountService;
    private final MemberService memberService;


    public ResponseEntity<?> startTodo(Long todoId) {
        try {
            Todo todo = todoRepository.findById(todoId).orElseThrow(
                    () -> new IllegalArgumentException("해당 ID의 할 일이 존재하지 않습니다."));

            TodoLog todoLog = new TodoLog();
            todoLog.setTodo(todo);
            todoLog.setStartTime(LocalDateTime.now());
            todoLog.setIsCompleted(false);
            todoLogRepository.save(todoLog);

            List<Todo> relatedTodos = todo.getRoutine().getTodos();
            boolean isFirstTodo = relatedTodos.getFirst().getId().equals(todoId);

            // 첫 할 일인 경우 루틴 로그 생성
            if(isFirstTodo) {
                RoutineLog routineLog = new RoutineLog();
                routineLog.setRoutine(todo.getRoutine());
                routineLog.setStartTime(LocalDateTime.now());
                routineLog.setIsCompleted(false);
                routineLogRepository.save(routineLog);
            }
            return ResponseEntity.ok(Map.of(
                    "message", "할 일이 시작되었습니다.",
                    "todoLogId", todoLog.getId()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "할 일 시작에 실패했습니다: " + e.getMessage()));
        }

    }

    public ResponseEntity<?> endTodo(Long todoId) {
        try {
            Todo todo = todoRepository.findById(todoId).orElseThrow(
                    () -> new IllegalArgumentException("해당 ID의 할 일이 존재하지 않습니다."));

            TodoLog todoLog = todoLogRepository.findByTodoAndIsCompleted(todo, false).orElseThrow(
                    () -> new IllegalArgumentException("해당 ID의 할 일 로그가 존재하지 않습니다."));

            todoLog.setEndTime(LocalDateTime.now());
            todoLog.setIsCompleted(true);


            Routine routine = todo.getRoutine();

            // 할 일 완료 시 적금에 시간 적립 및 로그 생성
            Member member = memberService.getMember();
            Long accountId = routine.getAccount().getId();
            Integer depositTime = (int) Duration.between(todoLog.getStartTime(), todoLog.getEndTime()).toMinutes();
            accountService.depositAccount(member, accountId, depositTime);


            todoLogRepository.save(todoLog); // 로그인 안 한 상태에서 할 일 완료 시 로그 저장 방지 위해 memberService 다음으로 위치

            if(!routine.getIsAchieved()) {
                routine.setIsAchieved(true);
                routineRepository.save(routine);
            }
            List<Todo> relatedTodos = todo.getRoutine().getTodos();
            boolean isLastTodo = relatedTodos.getLast().getId().equals(todo.getId());

            // 마지막 할 일인 경우 루틴 로그 종료 처리
            if(isLastTodo) {
                RoutineLog routineLog = routineLogRepository.findByRoutineAndIsCompleted(todo.getRoutine(), false).orElseThrow(
                        () -> new IllegalArgumentException("해당 루틴의 진행 중인 로그가 존재하지 않습니다."));
                routineLog.setEndTime(LocalDateTime.now());
                routineLog.setIsCompleted(true);
                routineLogRepository.save(routineLog);
            }
            return ResponseEntity.ok(Map.of("message", "할 일이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "할 일 완료에 실패했습니다: " + e.getMessage()));
        }
    }

    public ResponseEntity<?> getTodoCategory() {
        Map<String, String> response = new HashMap<>();
        response.put("english", "영어");
        response.put("math", "수학");
        response.put("science", "과학");
        response.put("history", "역사");
        response.put("art", "미술");
        response.put("music", "음악");
        return ResponseEntity.ok(response);
    }
}
