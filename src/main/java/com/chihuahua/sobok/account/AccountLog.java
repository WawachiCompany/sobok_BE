package com.chihuahua.sobok.account;

import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
public class AccountLog {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "FK_account_log_account"))
  private Account account;

  private Integer depositTime;
  private Integer balance;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
