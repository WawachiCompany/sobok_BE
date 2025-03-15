package com.chihuahua.sobok.survey;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SurveyRequestDto {
    private String spareTpo; // 자투리 시간 유형 (예: "출근길", "대기시간")
    private List<String> spareTime; // 자투리 시간 목록 (예: ["06:00-08:00", "18:00-20:00"])
    private String preference1; // 루틴 속성 1
    private String preference2; // 루틴 속성 2
    private String preference3; // 루틴 속성 3
    private List<String> likeOption; // 취미
    private String extraRequest; // 추가 요청
}
