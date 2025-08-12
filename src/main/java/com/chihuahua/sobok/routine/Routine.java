package com.chihuahua.sobok.routine;


import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.todo.Todo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
public class Routine {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "FK_routine_account"))
  @JsonBackReference
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_routine_member"))
  private Member member;

  private String title;
  private LocalTime startTime; // 형식: 14:00
  private LocalTime endTime;
  private Long duration; // 단위: 분

  @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private List<RoutineDay> routineDays = new ArrayList<>();

  @CreationTimestamp
  private LocalDateTime createdAt;

  private Boolean isSuspended = false; // 보류 여부

  private Boolean isAchieved = false; // 달성 여부(하나라도 완료했는지)

  private Boolean isCompleted = false; // 완료 여부(모든 할 일의 시간이 90퍼를 넘겼는지)

  private Boolean isEnded = false; // 종료 여부(적금 만기 또는 삭제)

  @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  List<Todo> todos = new ArrayList<>();

  private Boolean isAiRoutine; // AI 루틴 여부

  // 헬퍼 메서드: day 추가
  public void addDay(String day) {
    RoutineDay routineDay = new RoutineDay();
    routineDay.setDay(day);
    routineDay.setRoutine(this);
    this.routineDays.add(routineDay);
  }

  // 헬퍼 메서드: days 리스트 설정
  public void setDays(List<String> days) {
    this.routineDays.clear();
    if (days != null) {
      for (String day : days) {
        addDay(day);
      }
    }
  }

  // 헬퍼 메서드: days 리스트 조회
  public List<String> getDays() {
    return this.routineDays.stream()
        .map(RoutineDay::getDay)
        .toList();
  }

  // Routine에 Todo 추가
  public void addTodo(Todo todo) {
    if (!todos.contains(todo)) {
      todos.add(todo);
      todo.setRoutine(this); // Todo의 Routine 설정
    }
  }

  // Routine에서 Todo 제거
  public void removeTodo(Todo todo) {
    if (todos.contains(todo)) {
      todos.remove(todo);
      todo.setRoutine(null); // Todo의 Routine 해제
    }
  }

  // Routine에 Member 설정
  public void setMember(Member member) {
    if (this.member != null) {
      this.member.getRoutines().remove(this); // 기존 Member에서 이 Routine 제거
    }
    this.member = member; // member를 null로 설정 또는 새로운 멤버로 설정
    if (member != null && !member.getRoutines().contains(this)) {
      member.getRoutines().add(this); // 새로운 Member에 이 Routine 추가
    }
  }

  // Routine에 Account 설정
  public void setAccount(Account account) {
    // 같은 계정이면 아무것도 하지 않음
    if (this.account == account) {
      return;
    }

    // 기존 계정에서 연결 해제
    Account oldAccount = this.account;
    this.account = null;
    if (oldAccount != null && oldAccount.getRoutines() != null) {
      oldAccount.getRoutines().remove(this);
    }

    // 새 계정에 연결
    this.account = account;
    if (account != null && account.getRoutines() != null && !account.getRoutines().contains(this)) {
      account.getRoutines().add(this);
    }

  }


}
