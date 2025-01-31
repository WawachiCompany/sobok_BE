package com.apple.sobok.member;


import com.apple.sobok.routine.Routine;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
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
    private Integer point;
    private LocalDateTime createdAt;
    private Boolean isOauth;
    private Boolean isPremium;
    private Integer consecutiveAchieveCount;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Routine> routines;
}
