package com.apple.sobok.account;


import com.apple.sobok.member.Member;
import com.apple.sobok.routine.Routine;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;

    private String title;
    private String target;
    private Integer balance; // 단위: 분
    private Boolean isPublic;


    private Integer time; // 단위: 분
    private Integer duration; // 단위: 개월

    private Boolean isExpired;
    private LocalDate createdAt;

    private Boolean isValid; //활성화 요건 갖추었는지 확인
    private Float interest; // 이율
    private Long interestBalance; // 이자 잔액

    private LocalDate expiredAt; // 만기된 날짜
    private LocalDateTime updatedAt; // 수정된 시간

    @OneToMany(mappedBy = "account")
    @JsonManagedReference
    private List<Routine> routines = new ArrayList<>();

}
