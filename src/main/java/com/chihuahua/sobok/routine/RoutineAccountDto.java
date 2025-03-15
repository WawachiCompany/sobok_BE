package com.chihuahua.sobok.routine;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class RoutineAccountDto {
    private Long id;
    private String title;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long duration;
}
