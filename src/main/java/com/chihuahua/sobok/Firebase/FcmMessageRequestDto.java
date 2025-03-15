package com.chihuahua.sobok.Firebase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmMessageRequestDto {
    private String token;
    private String title;
    private String body;
}
