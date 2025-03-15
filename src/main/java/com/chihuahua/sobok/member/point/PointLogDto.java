package com.chihuahua.sobok.member.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class PointLogDto {
    private Long id;

    private Integer point;
    private Integer balance;
    private String category;
    private String description;
    private LocalDateTime createdAt;

    public PointLogDto(Long id, Integer point, Integer balance, String category, String description, LocalDateTime createdAt) {
        this.id = id;
        this.point = point;
        this.balance = balance;
        this.category = category;
        this.description = description;
        this.createdAt = createdAt;
    }
}

