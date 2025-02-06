package com.apple.sobok.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountLogRepository extends JpaRepository<AccountLog, Long> {

    List<AccountLog> findByAccount(Account account);
    void deleteAllByAccount(Account account);
}
