package com.chihuahua.sobok.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class InterestLog {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  private String targetYearMonth; // 연월 (YYYY-MM)
  private Long accountId; // 계좌 ID
  private Integer interest; // 이자 금액

}
