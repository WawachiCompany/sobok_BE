package com.chihuahua.sobok.account;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private Member member;

    private String title;
//    private String target;
    private Integer balance; // 단위: 분
//    private Boolean isPublic;


    private Integer time; // 단위: 분
    private Integer duration; // 단위: 개월

    private Boolean isExpired; // 만기 여부
    private Boolean isEnded; // 종료 여부
    private LocalDate createdAt;

    private Boolean isValid; // 활성화 요건 갖추었는지 확인
    private Float interest; // 이율
    private Integer interestBalance; // 이자 잔액

    private LocalDate expiredAt; // 만기된 날짜
    private LocalDateTime updatedAt; // 수정된 시간

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Routine> routines = new ArrayList<>();

    // Account에 Member 설정
    public void setMember(Member member) {
        if (this.member != null) {
            this.member.getAccounts().remove(this); // 기존 Member에서 제거
        }
        this.member = member;
        if (member != null && !member.getAccounts().contains(this)) {
            member.getAccounts().add(this); // 새로운 Member에 추가
        }
    }

    // Account에 Routine 추가
    public void addRoutine(Routine routine) {
        if (!routines.contains(routine)) {
            routines.add(routine);
            routine.setAccount(this); // Routine의 Account 설정
        }
    }

    // Account에서 Routine 제거
    public void removeRoutine(Routine routine) {
        if (routines.contains(routine)) {
            routines.remove(routine);
            routine.setAccount(null); // Routine의 Account 해제
        }
    }


}
