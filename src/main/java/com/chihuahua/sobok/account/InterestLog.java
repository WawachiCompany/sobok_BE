package com.chihuahua.sobok.account;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

  @ManyToOne
  @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "FK_interest_log_account"))
  private Account account; // 계좌 참조

  private Integer interest; // 이자 금액

}
