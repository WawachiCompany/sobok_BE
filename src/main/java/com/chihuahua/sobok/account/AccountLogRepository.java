package com.chihuahua.sobok.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountLogRepository extends JpaRepository<AccountLog, Long> {

    List<AccountLog> findByAccount(Account account);
    void deleteAllByAccount(Account account);
    List<AccountLog> findByAccountAndCreatedAtBetween(Account account, LocalDateTime start, LocalDateTime end);
}
