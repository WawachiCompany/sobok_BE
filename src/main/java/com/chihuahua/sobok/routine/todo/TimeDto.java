package com.chihuahua.sobok.routine.todo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeDto {
    private LocalTime startTime;
    private LocalTime endTime;
}
