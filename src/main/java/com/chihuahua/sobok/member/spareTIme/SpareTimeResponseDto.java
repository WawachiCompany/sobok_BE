package com.chihuahua.sobok.member.spareTIme;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SpareTimeResponseDto {
    private Long id;
    private String title;
    private String startTime;
    private String endTime;
    private Long duration;
    private List<String> days;
}
