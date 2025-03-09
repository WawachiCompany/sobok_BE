package com.apple.sobok.statistics.report;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class MonthlyUserReport {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String targetYearMonth;

    private Long totalAccumulatedTime;
    private Long AverageAccumulatedTime;


}
