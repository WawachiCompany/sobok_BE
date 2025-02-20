package com.apple.sobok.statistics;

import com.apple.sobok.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class DailyAchieve {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate date;
    private String status; // 달성 여부(All_ACHEIVED, SOME_ACHIEVED, NONE_ACHIEVED, NO_ROUTINE)
}
