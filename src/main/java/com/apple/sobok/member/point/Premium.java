package com.apple.sobok.member.point;


import com.apple.sobok.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Premium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    private LocalDate startAt;
    private LocalDate endAt;
//    private Boolean isAutoRenewal;
}
