package com.apple.sobok.member;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
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
}
