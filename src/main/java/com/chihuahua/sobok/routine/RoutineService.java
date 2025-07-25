package com.chihuahua.sobok.routine;


import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.account.AccountRepository;
import com.chihuahua.sobok.account.AccountService;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.routine.todo.Category;
import com.chihuahua.sobok.routine.todo.CategoryRepository;
import com.chihuahua.sobok.routine.todo.Todo;
import com.chihuahua.sobok.routine.todo.TodoLog;
import com.chihuahua.sobok.routine.todo.TodoLogRepository;
import com.chihuahua.sobok.routine.todo.TodoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoutineService {

  private static final Logger logger = LoggerFactory.getLogger(RoutineService.class);

  @PersistenceContext
  private EntityManager entityManager;

  private final AccountRepository accountRepository;
  private final RoutineRepository routineRepository;
  private final AccountService accountService;
  private final MemberRepository memberRepository;
  private final TodoLogRepository todoLogRepository;
  private final CategoryRepository categoryRepository;
  private final TodoRepository todoRepository;
  private final RoutineLogRepository routineLogRepository;

  @Transactional
  public void createRoutine(RoutineDto routineDto, Member member, String routineType) {
    Member persistedMember = memberRepository.findById(member.getId()).orElseThrow(
        () -> new IllegalArgumentException("해당 ID의 멤버가 존재하지 않습니다."));
    Routine routine = new Routine();

    // accountId가 null이거나 0이면 적금 연결하지 않음
    if (routineDto.getAccountId() != null && routineDto.getAccountId() > 0) {
      Account account = accountRepository.findById(
          routineDto.getAccountId()).orElseThrow(
          () -> new IllegalArgumentException("해당 적금이 존재하지 않습니다."));

      // 루틴과 적금 연결(Account 테이블의 헬퍼 메서드)
      account.addRoutine(routine);
      accountRepository.save(account);

    } else {
      routine.setAccount(null); // 적금 연결하지 않음
      routine.setIsSuspended(true); // 적금이 없으므로 루틴은 보류 상태로 설정
    }

    routine.setTitle(routineDto.getTitle());
    routine.setDays(routineDto.getDays());
    if (routineType.equals("self")) {
      routine.setIsAiRoutine(false);
    } else if (routineType.equals("ai")) {
      routine.setIsAiRoutine(true);
    }

    // 루틴과 멤버 연결(Member 테이블의 헬퍼 메서드)
    persistedMember.addRoutine(routine);

    routineRepository.save(routine);

    // Todo 생성 로직 추가
    if (routineDto.getTodos() != null && !routineDto.getTodos().isEmpty()) {
      List<Todo> todos = routineDto.getTodos().stream().map(todoDto -> {
        Todo todo = new Todo();

        todo.setTitle(todoDto.getTitle());
        todo.setCategory(todoDto.getCategory());

        // Category 테이블 추가
        if (categoryRepository.findByMemberAndCategory(member, todoDto.getCategory()).isEmpty()) {
          Category category = new Category();
          category.setMember(member);
          category.setCategory(todoDto.getCategory());
          categoryRepository.save(category);
        }
        todo.setStartTime(todoDto.getStartTime());
        todo.setEndTime(todoDto.getEndTime());
        todo.setDuration(
            Duration.between(todoDto.getStartTime(), todoDto.getEndTime()).toMinutes());
        todo.setLinkApp(todoDto.getLinkApp());
        routine.addTodo(todo);
        return todo;
      }).toList();

      // 루틴의 시작 시간과 종료 시간을 첫 번째 할일의 시작 시간과 마지막 할일의 종료 시간으로 설정
      routine.setStartTime(todos.getFirst().getStartTime());
      routine.setEndTime(todos.getLast().getEndTime());

      // 루틴의 duration을 할일들의 duration 합으로 설정
      long totalDuration = todos.stream().mapToLong(Todo::getDuration).sum();
      routine.setDuration(totalDuration);

      routineRepository.save(routine);

    }

    // 적금 활성화 여부 체크
    if (routine.getAccount() != null) {
      accountService.validateAccount(routine.getAccount());
    }
  }

  //루틴 이름, 연결 적금, 반복 요일 수정
  @Transactional
  public void updateRoutine(RoutineDto routineDto, Member member) {
    Optional<Routine> result = routineRepository.findByMemberAndId(member, routineDto.getId());
    if (result.isEmpty()) {
      throw new IllegalArgumentException("해당 루틴이 존재하지 않습니다.");
    }
    Routine routine = result.get();

    // accountId null이면 기존 적금 유지
    if (routineDto.getAccountId() != null && routineDto.getAccountId() > 0) {
      Account account = accountRepository.findById(routineDto.getAccountId())
          .orElseThrow(() -> new IllegalArgumentException("해당 적금이 존재하지 않습니다."));
      // 기존 적금과 변경할 적금이 같다면 패스
      if (routine.getAccount() != null && !routine.getAccount().getId()
          .equals(routineDto.getAccountId())) {
        // 기존 적금에서 루틴 제거
        routine.getAccount().removeRoutine(routine);
        // 새로운 적금에 루틴 추가
        account.addRoutine(routine);
        //적금 활성화 여부 체크
        accountService.validateAccount(account);
      }
    }

    routine.setTitle(routineDto.getTitle());
    routine.getDays().clear();
    routine.setDays(routineDto.getDays());
    routineRepository.save(routine);

    // 할 일은 할 일 수정 API에서 처리

  }

  @Transactional
  public void deleteRoutine(Member member, Long routineId) {
    // 1. 루틴 존재 여부 확인
    boolean exists = routineRepository.existsByIdAndMemberId(member.getId(), routineId);
    if (!exists) {
      throw new IllegalArgumentException("해당 루틴이 존재하지 않거나 사용자의 루틴이 아닙니다.");
    }

    // 2. 연관 관계 해제를 위한 별도 메서드 호출
    Long accountId = disconnectRoutineRelations(member.getId(), routineId);

    // 3. 벌크 삭제 연산 (cascade 없이 직접 삭제)
    performBulkDeletion(routineId);

    // 4. 계정 유효성 업데이트
    if (accountId != null) {
      updateAccountValidityAsync(accountId);
    }

    logger.info("Routine {} has been completely deleted for member {}", routineId, member.getId());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Long disconnectRoutineRelations(Long memberId, Long routineId) {
    try {
      // 영속성 컨텍스트 초기화
      entityManager.clear();

      // 필요한 데이터만 조회 (JOIN FETCH 사용)
      Routine routine = entityManager.createQuery(
              "SELECT r FROM Routine r " +
                  "LEFT JOIN FETCH r.account a " +
                  "WHERE r.id = :routineId AND r.member.id = :memberId", Routine.class)
          .setParameter("routineId", routineId)
          .setParameter("memberId", memberId)
          .getSingleResult();

      Long accountId = null;

      // Account 연관 관계 해제
      if (routine.getAccount() != null) {
        Account account = routine.getAccount();
        accountId = account.getId();

        // 네이티브 쿼리로 직접 연관 관계 해제
        entityManager.createNativeQuery(
                "UPDATE routine SET account_id = NULL WHERE id = :routineId")
            .setParameter("routineId", routineId)
            .executeUpdate();

        logger.info("Account 연관 관계 해제 완료: 루틴 ID {}, 계정 ID {}", routineId, accountId);
      }

      entityManager.flush();
      return accountId;

    } catch (Exception e) {
      logger.warn("루틴 관계 정리 중 오류 발생: {}", e.getMessage());
      return null;
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void performBulkDeletion(Long routineId) {
    try {
      // 순서대로 삭제 (외래키 제약 조건 고려)
      entityManager.createQuery(
              "DELETE FROM TodoLog tl WHERE tl.todo.routine.id = :routineId")
          .setParameter("routineId", routineId)
          .executeUpdate();

      entityManager.createQuery(
              "DELETE FROM RoutineLog rl WHERE rl.routine.id = :routineId")
          .setParameter("routineId", routineId)
          .executeUpdate();

      entityManager.createQuery(
              "DELETE FROM Todo t WHERE t.routine.id = :routineId")
          .setParameter("routineId", routineId)
          .executeUpdate();

      entityManager.createNativeQuery(
              "DELETE FROM routine_days WHERE routine_id = :routineId")
          .setParameter("routineId", routineId)
          .executeUpdate();

      entityManager.createQuery(
              "DELETE FROM Routine r WHERE r.id = :routineId")
          .setParameter("routineId", routineId)
          .executeUpdate();

      entityManager.flush();

    } catch (Exception e) {
      logger.error("벌크 삭제 중 오류 발생: {}", e.getMessage());
      throw new RuntimeException("루틴 삭제 중 오류가 발생했습니다.", e);
    }
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAccountValidityAsync(Long accountId) {
    try {
      // 새로운 트랜잭션에서 계정 조회 및 유효성 검사
      entityManager.clear();
      accountRepository.findById(accountId)
          .ifPresent(accountService::validateAccount);
    } catch (Exception e) {
      logger.warn("계정 유효성 업데이트 중 오류: {}", e.getMessage());
    }
  }


  public ResponseEntity<?> getTodayRoutine(Member member, String dateString) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate date = LocalDate.parse(dateString, formatter);
    String dayOfWeek = date.getDayOfWeek().toString();
    // 해당 멤버의 모든 루틴을 가져온 후 해당 요일이 포함된 루틴만 필터링
    List<Routine> result = routineRepository.findByMember(member).stream()
        .filter(routine -> routine.getDays().contains(dayOfWeek))
        .filter(routine -> !routine.getIsSuspended() && !routine.getIsEnded())
        .toList();

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
        "isSuspended", routine.getIsSuspended(),
        "isCompleted", routine.getIsEnded()
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
    response.put("accountTitle",
        routine.getAccount() != null ? routine.getAccount().getTitle() : "");
    response.put("title", routine.getTitle());
    response.put("days", routine.getDays());
    response.put("startTime", routine.getStartTime());
    response.put("endTime", routine.getEndTime());
    response.put("duration", routine.getDuration());
    response.put("isSuspended", routine.getIsSuspended());
    response.put("isAchieved", routine.getIsAchieved());
    response.put("isEnded", routine.getIsEnded());
    response.put("todos", routine.getTodos().stream()
        .map(todo -> {
              Map<String, Object> todoMap = new HashMap<>();
              todoMap.put("id", todo.getId());
              todoMap.put("title", todo.getTitle() != null ? todo.getTitle() : "");
              todoMap.put("startTime", todo.getStartTime());
              todoMap.put("endTime", todo.getEndTime());
              todoMap.put("duration", todo.getDuration());
              todoMap.put("linkApp", todo.getLinkApp() != null ? todo.getLinkApp() : "");
              todoMap.put("isCompleted", todo.getIsCompleted());
              todoMap.put("category", todo.getCategory() != null ? todo.getCategory() : "");
              return todoMap;
            }
        )
        .toList());
    return response;
  }

  public ResponseEntity<?> getTodayWillRoutine(Member member) {
    LocalDate date = LocalDate.now();
    String dayOfWeek = date.getDayOfWeek().toString();
    List<Routine> routines = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
    if (routines == null) {
      return ResponseEntity.ok(Map.of("message", "오늘 완료하지 않은 루틴이 없습니다."));
    }

    List<Map<String, Object>> result = routines.stream()
        .filter(routine -> routine != null && !routine.getIsCompleted())
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
    response.put("accountTitle",
        routine.getAccount() != null ? routine.getAccount().getTitle() : "");
    response.put("title", routine.getTitle());
    response.put("startTime", routine.getStartTime());
    response.put("endTime", routine.getEndTime());
    response.put("duration", routine.getDuration());
    response.put("todos", routine.getTodos() != null ?
        routine.getTodos().stream()
            .filter(todo -> todo != null && !todo.getIsCompleted())
            .map(todo -> Map.of(
                "id", todo.getId(),
                "title", todo.getTitle() != null ? todo.getTitle() : "",
                "startTime", todo.getStartTime(),
                "endTime", todo.getEndTime(),
                "duration", todo.getDuration(),
                "linkApp", todo.getLinkApp() != null ? todo.getLinkApp() : "",
                "category", todo.getCategory() != null ? todo.getCategory() : ""
            ))
            .toList()
        : List.of());
    return response;
  }

  public ResponseEntity<?> getTodayCompletedTime(Member member) {
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);

    List<TodoLog> completedLogs = todoLogRepository.findAllByMemberAndIsCompletedAndEndTimeBetween(
        member, startOfDay, endOfDay);
    // 할 일 아이디 별 duration 합계
    Map<Long, Long> todoDurationMap = completedLogs.stream()
        .collect(Collectors.groupingBy(todoLog -> todoLog.getTodo().getId(),
            Collectors.summingLong(TodoLog::getDuration)));
    long totalTime = completedLogs.stream()
        .mapToLong(TodoLog::getDuration)
        .sum();
    Map<String, Object> response = new HashMap<>();
    response.put("totalTime", totalTime);
    response.put("todoDurationMap", todoDurationMap);
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<?> getTodayDoneRoutine(Member member) {
    LocalDate date = LocalDate.now();
    String dayOfWeek = date.getDayOfWeek().toString();
    List<Routine> routines = routineRepository.findByUserIdAndDay(member.getId(), dayOfWeek);
    List<Map<String, Object>> result = routines.stream()
        .filter(Routine::getIsCompleted)
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
      Account account = routine.getAccount();
      if (!routine.getIsSuspended()) {
        routine.setIsSuspended(true);
        //루틴 보류 시 적금 연결도 해제
        account.removeRoutine(routine);
        routineRepository.save(routine);

        // 루틴 수정 후 주간 루틴 시간 계산
        calculateWeeklyRoutineTime(member);

        return ResponseEntity.ok(Map.of("message", "루틴이 중단되었습니다."));
      } else {
        routine.setIsSuspended(false);
        routineRepository.save(routine);

        // 루틴 수정 후 주간 루틴 시간 계산
        calculateWeeklyRoutineTime(member);

        return ResponseEntity.ok(Map.of("message", "루틴이 재개되었습니다."));
      }

    }
    return ResponseEntity.ok(Map.of("message", "해당 ID의 루틴이 없습니다."));
  }

  @Transactional
  public ResponseEntity<?> connectAccount(Member member, List<Long> routineIds, Long accountId) {
    try {
      // 적금 계좌 조회
      Account account = accountRepository.findById(accountId)
          .orElseThrow(() -> new EntityNotFoundException("해당 ID의 적금을 찾을 수 없습니다: " + accountId));

      // 적금 소유자 확인
      if (!account.getMember().getId().equals(member.getId())) {
        logger.warn("권한 없는 적금 연결 시도: 사용자 {}가 적금 ID {}에 접근", member.getId(), accountId);
        return ResponseEntity.status(403)
            .body(Map.of("message", "해당 적금에 대한 권한이 없습니다."));
      }

      // 루틴 목록 검증 및 연결
      List<Routine> connectedRoutines = new ArrayList<>();
      List<String> errors = new ArrayList<>();

      for (Long routineId : routineIds) {
        try {
          // 루틴 조회
          Routine routine = routineRepository.findById(routineId)
              .orElseThrow(() -> new EntityNotFoundException("해당 ID의 루틴을 찾을 수 없습니다: " + routineId));

          // 루틴 소유자 확인
          if (!routine.getMember().getId().equals(member.getId())) {
            errors.add("루틴 ID " + routineId + "에 대한 권한이 없습니다.");
            continue;
          }

          // 내장된 헬퍼 메서드를 사용하여 루틴의 적금 설정
          // 이 메서드는 내부적으로 양방향 관계를 처리합니다.
          routine.setAccount(account);

          connectedRoutines.add(routine);
          logger.debug("루틴-적금 연결 완료: 루틴 ID {}, 적금 ID {}", routineId, accountId);

        } catch (EntityNotFoundException e) {
          errors.add(e.getMessage());
        } catch (Exception e) {
          logger.error("루틴 연결 중 오류 발생: 루틴 ID {}, 적금 ID {}, 오류: {}",
              routineId, accountId, e.getMessage(), e);
          errors.add("루틴 ID " + routineId + " 연결 중 오류: " + e.getMessage());
        }
      }

      // 적금 유효성 검사 (필요한 경우)
      accountService.validateAccount(account);

      // 결과 반환
      Map<String, Object> response = new HashMap<>();
      response.put("message", "루틴과 적금 연결이 처리되었습니다.");
      response.put("connectedRoutineCount", connectedRoutines.size());
      response.put("connectedRoutineIds", connectedRoutines.stream()
          .map(Routine::getId)
          .collect(Collectors.toList()));

      if (!errors.isEmpty()) {
        response.put("warnings", errors);
      }

      logger.info("루틴-적금 연결 완료: {} 개의 루틴이 적금 ID {}와 연결됨",
          connectedRoutines.size(), accountId);
      return ResponseEntity.ok(response);

    } catch (EntityNotFoundException e) {
      logger.error("적금 연결 실패: {}", e.getMessage());
      return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      logger.error("적금 연결 중 예상치 못한 오류: {}", e.getMessage(), e);
      return ResponseEntity.status(500)
          .body(Map.of("message", "적금 연결 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }


  public void calculateWeeklyRoutineTime(Member member) {

    List<Routine> routines = routineRepository.findByMemberAndIsSuspendedAndIsEndedAndAccountIsExpired(
        member, false, false, false);
    long totalTime = routines.stream()
        .mapToLong(routine -> routine.getDuration() * routine.getDays().size())
        .sum();
    member.setWeeklyRoutineTime((int) totalTime);
    memberRepository.save(member);
  }

  @Transactional
  public ResponseEntity<?> completeRoutine(Member member, Long routineId) {
    try {
      // 루틴 조회 및 소유권 검증
      Routine routine = routineRepository.findById(routineId)
          .orElseThrow(() -> new EntityNotFoundException("해당 ID의 루틴이 존재하지 않습니다."));

      // 루틴 소유자 확인
      if (!routine.getMember().getId().equals(member.getId())) {
        logger.warn("권한 없는 접근 시도: 사용자 {}가 루틴 ID {}에 접근 시도", member.getId(), routineId);
        return ResponseEntity.status(403)
            .body(Map.of("message", "해당 루틴에 대한 권한이 없습니다."));
      }

      // 이미 종료된 루틴인지 확인
      if (routine.getIsEnded()) {
        logger.info("이미 완료된 루틴에 대한 완료 요청: 루틴 ID {}", routineId);
        return ResponseEntity.badRequest()
            .body(Map.of("message", "이미 완료된 루틴입니다."));
      }

      // 루틴과 적금 연결 해제 (양방향 관계)
      Account account = routine.getAccount();
      if (account != null) {
        logger.debug("루틴과 적금 연결 해제: 루틴 ID {}, 적금 ID {}", routineId, account.getId());
        account.removeRoutine(routine);
        // 적금이 변경되었으므로 적금 상태 유효성 검사
        accountService.validateAccount(account);
      }

      // 할일 상태 리셋
      routine.getTodos().forEach(todo -> todo.setIsCompleted(false));

      // 루틴 상태 업데이트
      routine.setIsAchieved(false);
      routine.setIsEnded(true);
      routine.setIsSuspended(false);

      logger.info("루틴 완료 처리 성공: 루틴 ID {}, 사용자 ID {}", routineId, member.getId());
      return ResponseEntity.ok(Map.of(
          "message", "루틴이 성공적으로 완료되었습니다.",
          "routineId", routine.getId(),
          "completedAt", LocalDateTime.now()
      ));

    } catch (EntityNotFoundException e) {
      logger.error("루틴을 찾을 수 없음: {}", e.getMessage());
      return ResponseEntity.status(404)
          .body(Map.of("message", e.getMessage()));
    } catch (Exception e) {
      // 구체적인 예외 정보와 스택 트레이스를 로그로 기록
      logger.error("루틴 완료 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);

      return ResponseEntity.status(500)
          .body(Map.of("message", "루틴 완료 처리 중 오류가 발생했습니다."));
    }
  }

}