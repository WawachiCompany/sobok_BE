package com.apple.sobok.member.point;


import com.apple.sobok.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PointLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;


    private Integer point;
    private Integer balance;
    private String category;
    private String description; // 내역 상세 있으면 넣고 없으면 빼기
    private LocalDateTime createdAt;
}
