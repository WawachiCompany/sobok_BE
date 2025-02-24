package com.apple.sobok.member.spareTIme;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SpareTimeDto {
    private Long id;
    private String title;
    private String startTime;
    private String endTime;
    private List<String> days;
}
