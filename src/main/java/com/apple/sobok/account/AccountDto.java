package com.apple.sobok.account;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountDto {
    private Long id;
    private String title;
//    private String target;
//    private Boolean isPublic;
    private Integer time; // 단위: 분
    private Integer duration; // 단위: 개월
    private Boolean isValid; //활성화 요건 갖추었는지 확인
    private Float interest; // 이율
    private List<Integer> routineIds; //

}
