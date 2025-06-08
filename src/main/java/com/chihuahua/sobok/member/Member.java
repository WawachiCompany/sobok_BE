package com.chihuahua.sobok.member;


import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.routine.Routine;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@ToString
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    private String username; // 로그인 시 사용하는 id
    private String password;
    private String name;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String birth;
    private Integer point = 0; // 포인트 (기본값: 0)

    @CreationTimestamp
    private LocalDateTime createdAt;
    private Boolean isOauth;
    private Boolean isPremium = false;
    private Integer consecutiveAchieveCount = 0; // 연속 달성 횟수
    private Integer premiumPrice = 9999; // 프리미엄 가격 (기본값: 9999원)

    private Integer totalAchievedTime = 0; // 총 달성 시간(분)
    private Integer totalAccountBalance = 0; // 총 적금 잔액(분)
    private Integer weeklyRoutineTime = 0; // 일주일 루틴 시간(분)

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Routine> routines;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Account> accounts;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "link_apps", joinColumns = @JoinColumn(name = "member_id"))
    private List<String> linkApps;

    // Member에 Account 추가
    public void addAccount(Account account) {
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        if (!accounts.contains(account)) {
            accounts.add(account);
            account.setMember(this); // Account의 Member 설정
        }
    }

    // Member에서 Account 제거
    public void removeAccount(Account account) {
        if (accounts != null && accounts.contains(account)) {
            accounts.remove(account);
            account.setMember(null); // Account의 Member 해제
        }
    }

    // Member에 Routine 추가
    public void addRoutine(Routine routine) {
        if (routines == null) {
            routines = new ArrayList<>();
        }
        routines.add(routine);
        routine.setMember(this);
    }

    // Member에서 Routine 제거
    public void removeRoutine(Routine routine) {
        routines.remove(routine);
        routine.setMember(null);
    }
}
