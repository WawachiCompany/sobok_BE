package com.chihuahua.sobok.member.point;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PremiumResponseDto {
    private LocalDate startAt;
    private LocalDate endAt;
}
