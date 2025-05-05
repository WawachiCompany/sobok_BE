package com.chihuahua.sobok.routine.todo;



import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.account.AccountService;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineLog;
import com.chihuahua.sobok.routine.RoutineLogRepository;
import com.chihuahua.sobok.routine.RoutineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoLogRepository todoLogRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineRepository routineRepository;
    private final AccountService accountService;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;


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

    public ResponseEntity<?> endTodo(Long todoLogId, Long duration) {
        try {
            TodoLog todoLog = todoLogRepository.findById(todoLogId).orElseThrow(
                    () -> new IllegalArgumentException("해당 ID의 할 일 로그가 존재하지 않습니다."));

            todoLog.setEndTime(LocalDateTime.now());
            todoLog.setDuration(duration);
            todoLog.setIsCompleted(true);

            Todo todo = todoLog.getTodo();
            Routine routine = todo.getRoutine();

            // 할 일 완료 시 적금에 시간 적립 및 로그 생성
            Member member = memberService.getMember();
            Long accountId = routine.getAccount().getId();

            accountService.depositAccount(member, accountId, Math.toIntExact(duration));


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
                routineLog.setDuration(Duration.between(routineLog.getStartTime(), routineLog.getEndTime()).toMinutes());
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

    public ResponseEntity<?> getTodayTodos() {
        List<Todo> todos = todoRepository.findByMemberAndDay(memberService.getMember(), LocalDateTime.now().getDayOfWeek().name());
        List<TodoDto> todoDtos = todos.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(todoDtos);
    }

    private TodoDto convertToDto(Todo todo) {
        TodoDto todoDto = new TodoDto();
        todoDto.setId(todo.getId());
        todoDto.setTitle(todo.getTitle());
        todoDto.setCategory(todo.getCategory());
        todoDto.setStartTime(todo.getStartTime());
        todoDto.setEndTime(todo.getEndTime());
        todoDto.setLinkApp(todo.getLinkApp());
        todoDto.setRoutineId(todo.getRoutine().getId());
        return todoDto;
    }

    public ResponseEntity<?> getClosestTodo(Member member) {
        LocalDateTime now = LocalDateTime.now();
        String today = now.getDayOfWeek().toString();

        // 오늘의 모든 할 일을 가져온 후 현재 시간 이후의 것들만 필터링
        List<Todo> todayTodos = todoRepository.findByMemberAndDay(member, today)
                .stream()
                .filter(todo -> !todo.getIsCompleted())
                .filter(todo -> {
                    LocalDateTime todoStartTime = LocalDateTime.of(now.toLocalDate(), todo.getStartTime());
                    return todoStartTime.isAfter(now);
                })
                .sorted(Comparator.comparing(todo ->
                        LocalDateTime.of(now.toLocalDate(), todo.getStartTime())
                ))
                .toList();

        if (todayTodos.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "오늘 남은 할 일이 없습니다."));
        }

        // 가장 가까운 할 일 반환
        Todo closestTodo = todayTodos.getFirst();
        return ResponseEntity.ok(convertToDto(closestTodo));

    }

    public ResponseEntity<?> getAllTodos() {
        List<Todo> todos = todoRepository.findAllByMember(memberService.getMember());
        List<TodoDto> todoDtos = todos.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(todoDtos);
    }

    public ResponseEntity<?> updateTodo(TodoDto todoDto) {
        Member member = memberService.getMember();
        Optional<Todo> todo = todoRepository.findById(todoDto.getId());
        if (todo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "해당 ID의 할 일이 존재하지 않습니다."));
        }
        Todo updatedTodo = todo.get();
        Routine routine = updatedTodo.getRoutine();
        List<Todo> todos = routine.getTodos();
        Account account = routine.getAccount();
        updatedTodo.setTitle(todoDto.getTitle());
        updatedTodo.setCategory(todoDto.getCategory());

        // Category 테이블 추가
        if(categoryRepository.findByMemberAndCategory(member, todoDto.getCategory()).isEmpty()) {
            Category category = new Category();
            category.setMember(member);
            category.setCategory(todoDto.getCategory());
            category.setCreatedAt(LocalDateTime.now());
            categoryRepository.save(category);
        }

        // 기존의 다른 할 일과의 중복 체크
        // #TODO
        List<Todo> existingTodos = todoRepository.findAllByMemberAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                member, todoDto.getEndTime(), todoDto.getStartTime());
        if (!existingTodos.isEmpty()) {
            throw new IllegalArgumentException("기존의 다른 할 일과 시간이 겹칩니다.");
        }

        updatedTodo.setStartTime(todoDto.getStartTime());
        updatedTodo.setEndTime(todoDto.getEndTime());
        updatedTodo.setDuration(Duration.between(todoDto.getStartTime(), todoDto.getEndTime()).toMinutes());
        updatedTodo.setLinkApp(todoDto.getLinkApp());
        updatedTodo.setIsCompleted(false);

        // 루틴의 시작 시간과 종료 시간을 첫 번째 할일의 시작 시간과 마지막 할일의 종료 시간으로 설정
        routine.setStartTime(todos.getFirst().getStartTime());
        routine.setEndTime(todos.getLast().getEndTime());

        // 루틴의 duration을 할일들의 duration 합으로 설정
        long totalDuration = todos.stream().mapToLong(Todo::getDuration).sum();
        routine.setDuration(totalDuration);

        routineRepository.save(routine);
        todoRepository.save(updatedTodo);

        //적금 활성화 여부 체크
        if(account != null) {
            accountService.validateAccount(account);
        }

        return ResponseEntity.ok(Map.of("message", "할 일이 업데이트되었습니다."));
    }

    @Transactional
    public ResponseEntity<?> deleteTodo(Long todoId) {
        Optional<Todo> todo = todoRepository.findById(todoId);
        if (todo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "해당 ID의 할 일이 존재하지 않습니다."));
        }
        Todo deletedTodo = todo.get();
        Routine routine = deletedTodo.getRoutine();
        Account account = routine.getAccount();

        // 루틴 할 일 삭제(헬퍼 메서드)
        routine.removeTodo(deletedTodo);
        routineRepository.save(routine);
        todoRepository.delete(deletedTodo);

        List<Todo> todos = routine.getTodos();

        // 루틴의 시작 시간과 종료 시간을 첫 번째 할일의 시작 시간과 마지막 할일의 종료 시간으로 설정
        if (!todos.isEmpty()) {
            routine.setStartTime(todos.getFirst().getStartTime());
            routine.setEndTime(todos.getLast().getEndTime());

            // 루틴의 duration을 할일들의 duration 합으로 설정
            long totalDuration = todos.stream().mapToLong(Todo::getDuration).sum();
            routine.setDuration(totalDuration);
        } else {
            routine.setStartTime(null);
            routine.setEndTime(null);
            routine.setDuration(0L);
        }

        routineRepository.save(routine);

        //적금 활성화 여부 체크
        if(account != null) {
            accountService.validateAccount(account);
        }

        return ResponseEntity.ok(Map.of("message", "할 일이 삭제되었습니다."));
    }

    @Transactional(readOnly = true)
    public boolean checkOverlap(Member member, OverlapTimeCheckDto overlapTimeCheckDto) {
        List<Todo> overlappingTodos = todoRepository.findOverlappingTodos(
                member,
                overlapTimeCheckDto.getDays(),
                overlapTimeCheckDto.getStartTime(),
                overlapTimeCheckDto.getEndTime()
        );
        return !overlappingTodos.isEmpty();
    }



//    public boolean checkOverlap(Member member, TimeDto timeDto) {
//        // 기존의 다른 할 일과의 중복 체크 (요일 반영)
//        List<Todo> existingTodos = todoRepository.findAllByMemberAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
//                member, timeDto.getEndTime(), timeDto.getStartTime());
//        return !existingTodos.isEmpty();
//    }
}
