package com.chihuahua.sobok.routine.todo;

import com.chihuahua.sobok.routine.Routine;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Todo {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "routine_id")
    private Routine routine;

    private String title;
    private String category;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long duration;
    private String linkApp;
    private Boolean isCompleted;
}
