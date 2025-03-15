package com.chihuahua.sobok.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto {
    private String username;
    private String password;
    private String name;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String birth;
}