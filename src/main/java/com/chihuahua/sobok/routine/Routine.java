package com.chihuahua.sobok.routine;


import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.todo.Todo;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonBackReference
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;

    private String title;
    private LocalTime startTime; // 형식: 14:00
    private LocalTime endTime;
    private Long duration; // 단위: 분

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "routine_days", joinColumns = @JoinColumn(name = "routine_id"))
    @Column(name = "day")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<String> days; // 요일 리스트(월 ~ 일)

    private LocalDateTime createdAt;

    private Boolean isSuspended; // 보류 여부

    private Boolean isAchieved; // 달성 여부(하나라도 완료했는지)

    private Boolean isEnded; // 종료 여부(적금 만기 또는 삭제)

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<Todo> todos = new ArrayList<>();

    private Boolean isAiRoutine; // AI 루틴 여부

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
        if (this.account != null) {
            this.account.getRoutines().remove(this); // 기존 Account에서 제거
        }
        this.account = account;
        if (account != null && !account.getRoutines().contains(this)) {
            account.getRoutines().add(this); // 새로운 Account에 추가
        }
    }


}



