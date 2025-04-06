package com.chihuahua.sobok.routine;


import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.account.AccountRepository;
import com.chihuahua.sobok.account.AccountService;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.routine.todo.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineService {

    private final AccountRepository accountRepository;
    private final RoutineRepository routineRepository;
    private final TodoRepository todoRepository;
    private final AccountService accountService;
    private final MemberRepository memberRepository;
    private final TodoLogRepository todoLogRepository;
    private final CategoryRepository categoryRepository;
    private final RoutineLogRepository routineLogRepository;

    @Transactional
    public void createRoutine(RoutineDto routineDto, Member member, String routineType) {
        Member persistedMember = memberRepository.findById(member.getId()).orElseThrow(
                () -> new IllegalArgumentException("해당 ID의 멤버가 존재하지 않습니다."));
        Routine routine = new Routine();
        Account account = accountRepository.findById(
                routineDto.getAccountId()).orElseThrow(
                () -> new IllegalArgumentException("해당 적금이 존재하지 않습니다."));
        routine.setMember(persistedMember);
        routine.setAccount(account);
        routine.setTitle(routineDto.getTitle());
        routine.setDays(routineDto.getDays());
        routine.setCreatedAt(LocalDateTime.now());
        routine.setIsSuspended(false);
        routine.setIsAchieved(false);
        routine.setIsEnded(false);
        if(routineType.equals("self")) {
            routine.setIsAiRoutine(false);
        }
        else if(routineType.equals("ai")) {
            routine.setIsAiRoutine(true);
        }
        routineRepository.save(routine);

        // Todo 생성 로직 추가
        if (routineDto.getTodos() != null && !routineDto.getTodos().isEmpty()) {
            List<Todo> todos = routineDto.getTodos().stream().map(todoDto -> {
                Todo todo = new Todo();
                todo.setRoutine(routine);
                todo.setTitle(todoDto.getTitle());
                todo.setCategory(todoDto.getCategory());

                // Category 테이블 추가
                if(categoryRepository.findByMemberAndCategory(member, todoDto.getCategory()).isEmpty()) {
                    Category category = new Category();
                    category.setMember(member);
                    category.setCategory(todoDto.getCategory());
                    category.setCreatedAt(LocalDateTime.now());
                    categoryRepository.save(category);
                }
                todo.setStartTime(todoDto.getStartTime());
                todo.setEndTime(todoDto.getEndTime());
                todo.setDuration(Duration.between(todoDto.getStartTime(), todoDto.getEndTime()).toMinutes());
                todo.setLinkApp(todoDto.getLinkApp());
                todo.setIsCompleted(false);
                todo.setCategory(todoDto.getCategory());
                return todo;
            }).collect(Collectors.toList());

            // 루틴의 시작 시간과 종료 시간을 첫 번째 할일의 시작 시간과 마지막 할일의 종료 시간으로 설정
            routine.setStartTime(todos.getFirst().getStartTime());
            routine.setEndTime(todos.getLast().getEndTime());

            // 루틴의 duration을 할일들의 duration 합으로 설정
            long totalDuration = todos.stream().mapToLong(Todo::getDuration).sum();
            routine.setDuration(totalDuration);

            routineRepository.save(routine);
            todoRepository.saveAll(todos);

        }

        //적금 활성화 여부 체크
        accountService.validateAccount(account);
    }

    public void updateRoutine(RoutineDto routineDto, Member member, Long routineId) {
        var result = routineRepository.findByMemberAndId(member, routineId);
        if(result.isEmpty()) {
            throw new IllegalArgumentException("해당 루틴이 존재하지 않습니다.");
        }
        Routine routine = result.get();
        Account account = accountRepository.findById(
                routineDto.getAccountId()).orElseThrow(
                () -> new IllegalArgumentException("해당 적금이 존재하지 않습니다."));
        routine.setAccount(account);
        routine.setTitle(routineDto.getTitle());
        routine.setDays(routineDto.getDays());
        routineRepository.save(routine);

        // Todo 업데이트 로직 추가
        List<Todo> todos = todoRepository.findByRoutine(routine);
        todoRepository.deleteAll(todos);
        if (routineDto.getTodos() != null && !routineDto.getTodos().isEmpty()) {
            todos = routineDto.getTodos().stream().map(todoDto -> {
                Todo todo = new Todo();
                todo.setRoutine(routine);
                todo.setTitle(todoDto.getTitle());
                todo.setCategory(todoDto.getCategory());
                todo.setStartTime(todoDto.getStartTime());
                todo.setEndTime(todoDto.getEndTime());
                todo.setDuration(Duration.between(todoDto.getStartTime(), todoDto.getEndTime()).toMinutes());
                todo.setLinkApp(todoDto.getLinkApp());
                todo.setIsCompleted(false);
                return todo;
            }).collect(Collectors.toList());

            // 루틴의 시작 시간과 종료 시간을 첫 번째 할일의 시작 시간과 마지막 할일의 종료 시간으로 설정
            routine.setStartTime(todos.getFirst().getStartTime());
            routine.setEndTime(todos.getLast().getEndTime());

            // 루틴의 duration을 할일들의 duration 합으로 설정
            long totalDuration = todos.stream().mapToLong(Todo::getDuration).sum();
            routine.setDuration(totalDuration);

            todoRepository.saveAll(todos);
        }

        //적금 활성화 여부 체크
        accountService.validateAccount(account);
    }

    @Transactional
    public void deleteRoutine(Member member, Long routineId) {
        var result = routineRepository.findByMemberAndId(member, routineId);
        if(result.isEmpty()) {
            throw new IllegalArgumentException("해당 루틴이 존재하지 않습니다.");
        }
        Routine routine = result.get();
        Account account = routine.getAccount();

        // 루틴 로그 제거
        routineLogRepository.deleteAllByRoutine(routine);


        // todolog 및 Todo 삭제 로직 추가
        List<Todo> todos = todoRepository.findByRoutine(routine);
        if (!todos.isEmpty()) {
            for (Todo todo : todos) {
                todoLogRepository.deleteTodoLogByTodo(todo);
            }
        }
        routine.getTodos().removeAll(todos);
        routineRepository.save(routine);

        // todoRepository에서 todo 삭제
        todoRepository.deleteAll(todos);

        // Member 테이블에서 루틴 제거
        member.getRoutines().remove(routine);
        memberRepository.save(member);

        // Account 테이블에서 루틴 제거
        if(account != null){
            account.getRoutines().remove(routine);
            accountRepository.save(account);
            //적금 활성화 여부 체크
            accountService.validateAccount(account);
        }

        //루틴에 연결된 적금 및 멤버 제거
        routine.setAccount(null);
        routine.setMember(null);
        routineRepository.save(routine);

        routineRepository.delete(routine);
    }

    public ResponseEntity<?> getTodayRoutine(Member member, String dateString) {
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

    private Map<String, Object> convertToMapCal(Routine routine) {
        return Map.of(
                "id", routine.getId(),
                "title", routine.getTitle(),
                "accountTitle", routine.getAccount() != null ? routine.getAccount().getTitle() : "",
                "startTime", routine.getStartTime(),
                "endTime", routine.getEndTime(),
                "duration", routine.getDuration()
        );
    }

    public ResponseEntity<?> getAllRoutine(Member member) {
        List<Routine> result = routineRepository.findByMember(member);
        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "루틴이 없습니다."));
        }
        List<Map<String, Object>> routines = result.stream()
                .map(this::convertToMapList)
                .toList();

        return ResponseEntity.ok(routines);
    }

    private Map<String, Object> convertToMapList(Routine routine) {
        return Map.of(
                "id", routine.getId(),
                "title", routine.getTitle(),
                "accountTitle", routine.getAccount() != null ? routine.getAccount().getTitle() : "",
                "duration", routine.getDuration(),
                "isSuspended", routine.getIsSuspended()
        );
    }

    public ResponseEntity<?> getRoutine(Long routineId) {
        var result = routineRepository.findById(routineId);
        if (result.isPresent()) {
            return ResponseEntity.ok(convertToMap(result.get()));
        }
        return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));
    }

    private Map<String, Object> convertToMap(Routine routine) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", routine.getId());
        response.put("accountTitle", routine.getAccount() != null ? routine.getAccount().getTitle() : "");
        response.put("title", routine.getTitle());
        response.put("days", routine.getDays());
        response.put("startTime", routine.getStartTime());
        response.put("endTime", routine.getEndTime());
        response.put("duration", routine.getDuration());
        response.put("isSuspended", routine.getIsSuspended());
        response.put("isAchieved", routine.getIsAchieved());
        response.put("isEnded", routine.getIsEnded());
        response.put("todos", routine.getTodos().stream()
                .map(todo -> Map.of(
                        "title", todo.getTitle(),
                        "startTime", todo.getStartTime(),
                        "endTime", todo.getEndTime(),
                        "duration", todo.getDuration(),
                        "linkApp", todo.getLinkApp(),
                        "isCompleted", todo.getIsCompleted(),
                        "category", todo.getCategory()
                ))
                .toList());
        return response;
    }

    public ResponseEntity<?> getTodayWillRoutine(Member member) {
        LocalDate date = LocalDate.now();
        String dayOfWeek = date.getDayOfWeek().toString();
        List<Routine> routines = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
        List<Map<String, Object>> result = routines.stream()
                .filter(routine -> !routine.getIsAchieved())
                .map(this::convertToMapTimer)
                .toList();
        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "오늘 완료하지 않은 루틴이 없습니다."));
        }
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> convertToMapTimer(Routine routine) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", routine.getId());
        response.put("accountTitle", routine.getAccount() != null ? routine.getAccount().getTitle() : "");
        response.put("title", routine.getTitle());
        response.put("startTime", routine.getStartTime());
        response.put("endTime", routine.getEndTime());
        response.put("duration", routine.getDuration());
        response.put("todos", routine.getTodos().stream()
                .filter(todo -> !todo.getIsCompleted())
                .map(todo -> Map.of(
                        "id", todo.getId(),
                        "title", todo.getTitle(),
                        "startTime", todo.getStartTime(),
                        "endTime", todo.getEndTime(),
                        "duration", todo.getDuration(),
                        "linkApp", todo.getLinkApp(),
                        "category", todo.getCategory()
                ))
                .toList());
        return response;
    }

    public ResponseEntity<?> getTodayCompletedTime(Member member) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<TodoLog> completedLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetWeen(member, startOfDay, endOfDay);
        long totalTime = completedLogs.stream()
                .mapToLong(log -> Duration.between(log.getStartTime(), log.getEndTime()).toMinutes())
                .sum();

        return ResponseEntity.ok(Map.of("totalTime", totalTime));
    }

    public ResponseEntity<?> getTodayDoneRoutine(Member member) {
        LocalDate date = LocalDate.now();
        String dayOfWeek = date.getDayOfWeek().toString();
        List<Routine> routines = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
        List<Map<String, Object>> result = routines.stream()
                .filter(Routine::getIsAchieved)
                .map(this::convertToMapList)
                .toList();
        if (result.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "오늘 완료한 루틴이 없습니다."));
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> getIsThereEndedRoutine(Member member) {
        List<Routine> routines = routineRepository.findByMemberAndIsEnded(member, true);
        if (routines.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "만료된 루틴이 없습니다."));
        }
        List<Map<String, Object>> result = routines.stream()
                .map(this::convertToMap)
                .toList();
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<?> suspendRoutine(Member member, Long routineId) {
        var result = routineRepository.findByMemberAndId(member, routineId);
        if (result.isPresent()) {
            Routine routine = result.get();
            if(!routine.getIsSuspended()) {
                routine.setIsSuspended(true);
                //루틴 보류 시 적금 연결도 해제
                routine.setAccount(null);
                routineRepository.save(routine);
                return ResponseEntity.ok(Map.of("message", "루틴이 중단되었습니다."));
            }
            else {
                routine.setIsSuspended(false);
                routineRepository.save(routine);
                return ResponseEntity.ok(Map.of("message", "루틴이 재개되었습니다."));
            }

        }
        return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));
    }

    public ResponseEntity<?> connectAccount(Member member, Long routineId, Long accountId) {
        var result = routineRepository.findByMemberAndId(member, routineId);
        if (result.isPresent()) {
            Routine routine = result.get();
            Account account = accountRepository.findById(accountId).orElseThrow(
                    () -> new IllegalArgumentException("해당 ID의 적금이 존재하지 않습니다."));
            routine.setAccount(account);
            routineRepository.save(routine);
            return ResponseEntity.ok(Map.of("message", "루틴이 적금과 연결되었습니다."));
        }
        else{
            throw new IllegalArgumentException("해당 ID의 루틴이 없습니다.");
        }
    }

    public void calculateWeeklyRoutineTime(Member member) {
        List<Routine> routines = routineRepository.findByMemberAndIsSuspendedAndIsEndedAndAccountIsExpired(member, false, false, false);
        long totalTime = routines.stream()
                .mapToLong(routine -> routine.getDuration() * routine.getDays().size())
                .sum();
        member.setWeeklyRoutineTime((int) totalTime);
        memberRepository.save(member);
    }



}
