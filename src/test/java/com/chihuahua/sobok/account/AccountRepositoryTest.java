package com.chihuahua.sobok.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2로 바꿔치기 금지
class AccountRepositoryTest {

  @Autowired
  private AccountRepository accountRepository;

  @Test
  void shouldReturnExpectedName_whenFindById() {
    // given: Liquibase test 시드에 미리 넣어둔 값
    Long targetId = 1L;
    String expectedName = "김철수1";

    // when
    Optional<Account> found = accountRepository.findById(targetId);

    // then
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo(expectedName);
  }

  @Test
  void shouldAddAccountBalance_whenDepositAccount() {
    // given: Liquibase test 시드에 미리 넣어둔 값
    Long targetId = 1L;
    int depositAmount = 1000;

    // 기존 계좌 조회 및 초기 잔액 저장
    Optional<Account> accountBefore = accountRepository.findById(targetId);
    assertThat(accountBefore).isPresent();
    int initialBalance = accountBefore.get().getBalance();

    // when: 계좌에 입금 (잔액 업데이트)
    Account account = accountBefore.get();
    account.setBalance(account.getBalance() + depositAmount);
    accountRepository.save(account);

    // then: 업데이트된 계좌 다시 조회하여 검증
    Optional<Account> accountAfter = accountRepository.findById(targetId);
    assertThat(accountAfter).isPresent();
    assertThat(accountAfter.get().getBalance()).isEqualTo(initialBalance + depositAmount);

    System.out.println("초기 잔액: " + initialBalance + "분");
    System.out.println("입금 금액: " + depositAmount + "분");
    System.out.println("최종 잔액: " + accountAfter.get().getBalance() + "분");
  }
}
