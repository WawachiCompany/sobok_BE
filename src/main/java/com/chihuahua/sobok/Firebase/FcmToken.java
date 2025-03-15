package com.chihuahua.sobok.Firebase;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class FcmToken {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String fcmToken;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
