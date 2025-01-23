package com.apple.sobok.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.apple.sobok.account.Account;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AccountLog {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private Integer depositTime;
    private LocalDateTime createdAt;
}
