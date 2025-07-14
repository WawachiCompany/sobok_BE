package com.chihuahua.sobok.testdata;

import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.account.AccountLog;
import com.chihuahua.sobok.account.AccountLogRepository;
import com.chihuahua.sobok.account.AccountRepository;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestComponent
public class SimpleMemberTestData {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private AccountLogRepository accountLogRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public Member createTestMember() {
    return createTestMember("test@example.com", "testuser", "Test User");
  }

  public Member createTestMember(String email, String username, String displayName) {
    Member member = new Member();
    member.setEmail(email);
    member.setUsername(username);
    member.setDisplayName(displayName);
    member.setPassword(passwordEncoder.encode("password123"));
    member.setPhoneNumber("010-1234-5678");
    member.setBirth("1990-01-01");
    member.setPoint(1000);
    member.setTotalAccountBalance(0);
    member.setTotalAchievedTime(0);
    member.setPremiumPrice(9999);
    member.setIsPremium(false);
    member.setIsOauth(false);
    return memberRepository.save(member);
  }

  public Account createTestAccount(Member member) {
    return createTestAccount(member, "테스트 적금", 600, 6);
  }

  public Account createTestAccount(Member member, String title, Integer time, Integer duration) {
    Account account = new Account();
    account.setTitle(title);
    account.setTime(time);
    account.setDuration(duration);
    account.setBalance(0);
    account.setInterestBalance(0);
    account.setInterest(5.0f);
    account.setIsValid(true);
    account.setIsExpired(false);
    account.setIsExtended(false);
    account.setIsEnded(false);
    account.setExpiredAt(LocalDate.now().plusMonths(duration));
    account.setMember(member);
    return accountRepository.save(account);
  }

  public AccountLog createTestAccountLog(Account account, Integer depositTime) {
    AccountLog accountLog = new AccountLog();
    accountLog.setAccount(account);
    accountLog.setDepositTime(depositTime);
    accountLog.setBalance(account.getBalance() + depositTime);
    accountLog.setCreatedAt(LocalDateTime.now());

    // 계정 잔액 업데이트
    account.setBalance(account.getBalance() + depositTime);
    accountRepository.save(account);

    return accountLogRepository.save(accountLog);
  }

  public void cleanupTestData() {
    accountLogRepository.deleteAll();
    accountRepository.deleteAll();
    memberRepository.deleteAll();
  }
}
