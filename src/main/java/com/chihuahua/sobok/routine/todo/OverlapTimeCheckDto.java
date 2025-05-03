package com.chihuahua.sobok.routine.todo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class OverlapTimeCheckDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private List<String> days;
}
