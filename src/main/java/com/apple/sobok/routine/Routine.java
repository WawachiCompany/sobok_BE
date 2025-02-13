package com.apple.sobok.routine;


import com.apple.sobok.account.Account;
import com.apple.sobok.member.Member;
import com.apple.sobok.routine.todo.Todo;
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
}



