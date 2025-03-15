package com.chihuahua.sobok.statistics.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SnowCard {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String targetYearMonth;

    private String snowCard;
}
