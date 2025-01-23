package com.apple.sobok.routine;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
public class AiRoutine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    @ElementCollection
    @CollectionTable(name = "ai_routine_days", joinColumns = @JoinColumn(name = "ai_routine_id"))
    @Column(name = "day")
    private List<Boolean> days; // 요일 리스트(일 ~ 토)
}
