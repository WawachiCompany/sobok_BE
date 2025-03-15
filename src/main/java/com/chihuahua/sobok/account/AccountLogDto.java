package com.chihuahua.sobok.account;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AccountLogDto {
    private Long id;
    private Integer depositTime;
    private Integer balance;
    private LocalDateTime createdAt;
}
