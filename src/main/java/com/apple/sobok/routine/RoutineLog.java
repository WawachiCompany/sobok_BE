package com.apple.sobok.routine;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class RoutineLog {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "routine_id")
    private Routine routine;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;

    private Boolean isCompleted;
}
