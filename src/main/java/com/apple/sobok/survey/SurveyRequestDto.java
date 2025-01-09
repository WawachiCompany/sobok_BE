package com.apple.sobok.survey;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyRequestDto {
    private String occupation; // 직업
    private List<String> spareTime; // 자투리 시간 목록 (예: ["06:00-08:00", "18:00-20:00"])
    private String preference1; // 루틴 속성 1
    private String preference2; // 루틴 속성 2
    private String preference3; // 루틴 속성 3
    private String likeOption; // 취미
}
