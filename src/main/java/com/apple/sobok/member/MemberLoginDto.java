package com.apple.sobok.member;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemberLoginDto {
    private String username;
    private String password;
    private String displayName;

}
