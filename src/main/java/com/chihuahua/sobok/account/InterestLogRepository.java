package com.chihuahua.sobok.account;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestLogRepository extends JpaRepository<InterestLog, Long> {

  List<InterestLog> findByAccountId(Long accountId);
}
