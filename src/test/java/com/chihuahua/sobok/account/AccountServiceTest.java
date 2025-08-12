package com.chihuahua.sobok.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chihuahua.sobok.exception.BadRequestException;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.member.MemberService;
import com.chihuahua.sobok.member.point.PointLog;
import com.chihuahua.sobok.member.point.PointLogRepository;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountLogRepository accountLogRepository;

  @Mock
  private InterestLogRepository interestLogRepository;

  @Mock
  private PointLogRepository pointLogRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RoutineRepository routineRepository;

  @Mock
  private MemberService memberService;

  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private AccountService accountService;

  private Member testMember;
  private Account testAccount;
  private Routine testRoutine1;
  private Routine testRoutine2;

  @BeforeEach
  void setUp() {
    // 테스트용 Member 생성
    testMember = new Member();
    testMember.setId(1L);
    testMember.setUsername("testuser");
    testMember.setName("테스트유저");
    testMember.setTotalAccountBalance(1000);
    testMember.setTotalAchievedTime(500);
    testMember.setPoint(5000);

    List<Account> accounts = new ArrayList<>();
    testMember.setAccounts(accounts);

    // 테스트용 Account 생성
    testAccount = new Account();
    testAccount.setId(1L);
    testAccount.setTitle("테스트 적금");
    testAccount.setBalance(1000);
    testAccount.setTime(1440); // 24시간 = 1440분
    testAccount.setDuration(12);
    testAccount.setMember(testMember);
    testAccount.setIsExpired(false);
    testAccount.setIsEnded(false);
    testAccount.setIsValid(false);
    testAccount.setIsExtended(false);
    testAccount.setExpiredAt(LocalDate.now().plusMonths(12));
    testAccount.setInterestBalance(1000);

    accounts.add(testAccount);

    // 테스트용 Routine 생성
    testRoutine1 = new Routine();
    testRoutine1.setId(1L);
    testRoutine1.setTitle("루틴1");
    testRoutine1.setDuration(30L);
    testRoutine1.setDays(Arrays.asList("월", "화", "수", "목", "금")); // 5일
    testRoutine1.setAccount(testAccount);
    testRoutine1.setIsEnded(false);

    testRoutine2 = new Routine();
    testRoutine2.setId(2L);
    testRoutine2.setTitle("루틴2");
    testRoutine2.setDuration(42L);
    testRoutine2.setDays(Arrays.asList("월", "화", "수", "목", "금", "토", "일")); // 7일
    testRoutine2.setAccount(testAccount);
    testRoutine2.setIsEnded(false);

    // Account에 Routine 연결
    List<Routine> routines = new ArrayList<>();
    routines.add(testRoutine1);
    routines.add(testRoutine2);
    testAccount.setRoutines(routines);
  }

  @Test
  @DisplayName("depositAccount - 정상적인 금액 입금 테스트")
  void depositAccount_Success() {
    // Given
    Long accountId = 1L;
    Integer depositAmount = 300;
    Integer expectedBalance = 1300; // 기존 1000 + 300

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(accountLogRepository.save(any(AccountLog.class)))
        .thenReturn(new AccountLog());

    // When
    Account result = accountService.depositAccount(testMember, accountId, depositAmount);

    // Then
    assertNotNull(result);
    assertEquals(expectedBalance, result.getBalance());
    assertEquals(500 + depositAmount, testMember.getTotalAchievedTime());

    // AccountLog 저장이 호출되었는지 확인
    verify(accountLogRepository, times(1)).save(argThat(log ->
        log.getAccount().equals(testAccount) &&
            log.getDepositTime().equals(depositAmount) &&
            log.getBalance().equals(expectedBalance)
    ));
  }

  @Test
  @DisplayName("depositAccount - 존재하지 않는 계좌 입금 시도 테스트")
  void depositAccount_AccountNotFound() {
    // Given
    Long accountId = 999L;
    Integer depositAmount = 300;

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> accountService.depositAccount(testMember, accountId, depositAmount)
    );

    assertEquals("해당 적금을 찾을 수 없습니다.", exception.getMessage());

    // AccountLog가 저장되지 않았는지 확인
    verify(accountLogRepository, never()).save(any(AccountLog.class));
  }

  @Test
  @DisplayName("depositAccount - 0원 입금 테스트")
  void depositAccount_ZeroAmount() {
    // Given
    Long accountId = 1L;
    Integer depositAmount = 0;
    Integer expectedBalance = 1000; // 변화 없음

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(accountLogRepository.save(any(AccountLog.class)))
        .thenReturn(new AccountLog());

    // When
    Account result = accountService.depositAccount(testMember, accountId, depositAmount);

    // Then
    assertNotNull(result);
    assertEquals(expectedBalance, result.getBalance());
    assertEquals(500 + depositAmount, testMember.getTotalAchievedTime());

    // AccountLog 저장이 호출되었는지 확인
    verify(accountLogRepository, times(1)).save(any(AccountLog.class));
  }

  @Test
  @DisplayName("depositAccount - 큰 금액 입금 테스트")
  void depositAccount_LargeAmount() {
    // Given
    Long accountId = 1L;
    Integer depositAmount = 10000;
    Integer expectedBalance = 11000; // 기존 1000 + 10000

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(accountLogRepository.save(any(AccountLog.class)))
        .thenReturn(new AccountLog());

    // When
    Account result = accountService.depositAccount(testMember, accountId, depositAmount);

    // Then
    assertNotNull(result);
    assertEquals(expectedBalance, result.getBalance());
    assertEquals(500 + depositAmount, testMember.getTotalAchievedTime());

    verify(accountLogRepository, times(1)).save(any(AccountLog.class));
  }

  @Test
  @DisplayName("depositAccount - Member의 totalAccountBalance 업데이트 테스트")
  void depositAccount_UpdateTotalAccountBalance() {
    // Given
    Long accountId = 1L;
    Integer depositAmount = 500;

    // 추가 계좌 생성 (총 잔액 계산 테스트용)
    Account anotherAccount = new Account();
    anotherAccount.setId(2L);
    anotherAccount.setBalance(2000);
    anotherAccount.setMember(testMember);

    List<Account> accounts = new ArrayList<>();
    accounts.add(testAccount);
    accounts.add(anotherAccount);
    testMember.setAccounts(accounts);

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(accountLogRepository.save(any(AccountLog.class)))
        .thenReturn(new AccountLog());

    // When
    Account result = accountService.depositAccount(testMember, accountId, depositAmount);

    // Then
    assertNotNull(result);
    assertEquals(1500, result.getBalance()); // 1000 + 500

    // totalAccountBalance가 올바르게 계산되었는지 확인 (1500 + 2000 = 3500)
    assertEquals(3500, testMember.getTotalAccountBalance());

    // totalAchievedTime이 올바르게 업데이트되었는지 확인
    assertEquals(1000, testMember.getTotalAchievedTime()); // 500 + 500
  }

  @Test
  @DisplayName("validateAccount - 루틴이 없을 때 비활성화")
  void validateAccount_NoRoutines() {
    // Given
    testAccount.setRoutines(new ArrayList<>());

    // When
    accountService.validateAccount(testAccount);

    // Then
    assertFalse(testAccount.getIsValid());
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("validateAccount - 루틴이 null일 때 비활성화")
  void validateAccount_NullRoutines() {
    // Given
    testAccount.setRoutines(null);

    // When
    accountService.validateAccount(testAccount);

    // Then
    assertFalse(testAccount.getIsValid());
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("validateAccount - 루틴 총 시간이 적금 시간과 일치할 때 활성화")
  void validateAccount_ValidRoutines() {
    // Given
    // testRoutine1: 30분 * 5일 * 4주 = 600분
    // testRoutine2: 42분 * 7일 * 4주 = 1176분
    // 총합: 1776분 (testAccount.time = 1440분으로 설정 필요)
    testAccount.setTime(1776);

    // When
    accountService.validateAccount(testAccount);

    // Then
    assertTrue(testAccount.getIsValid());
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("validateAccount - 루틴 총 시간이 적금 시간과 일치하지 않을 때 비활성화")
  void validateAccount_InvalidRoutines() {
    // Given
    // 루틴 총 시간(1776분)과 적금 시간(1440분)이 일치하지 않음

    // When
    accountService.validateAccount(testAccount);

    // Then
    assertFalse(testAccount.getIsValid());
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("validateAccount - duration이나 days가 null인 루틴은 제외")
  void validateAccount_IgnoreInvalidRoutines() {
    // Given
    testRoutine1.setDuration(null);
    testRoutine2.setDays(null);

    // When
    accountService.validateAccount(testAccount);

    // Then
    assertFalse(testAccount.getIsValid());
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("extendAccount - 정상 연장 처리")
  void extendAccount_Success() {
    // Given
    Long accountId = 1L;
    Integer extensionDuration = 3; // 3개월 연장
    LocalDate originalExpiredAt = testAccount.getExpiredAt();

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(memberRepository.save(any(Member.class))).thenReturn(testMember);
    when(pointLogRepository.save(any(PointLog.class))).thenReturn(new PointLog());

    // When
    accountService.extendAccount(testMember, accountId, extensionDuration);

    // Then
    assertEquals(originalExpiredAt.plusMonths(extensionDuration), testAccount.getExpiredAt());
    assertFalse(testAccount.getIsExpired());
    assertTrue(testAccount.getIsExtended());
    assertEquals(6000, testMember.getPoint()); // 5000 + 1000(이자)

    verify(memberRepository, times(1)).save(testMember);
    verify(pointLogRepository, times(1)).save(argThat(log ->
        log.getMember().equals(testMember) &&
            log.getPoint().equals(1000) &&
            log.getCategory().equals("적금 연장 전 이자 지급")
    ));
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("extendAccount - 이자가 0일 때 포인트 지급 없음")
  void extendAccount_NoInterest() {
    // Given
    Long accountId = 1L;
    Integer extensionDuration = 3;
    testAccount.setInterestBalance(0);

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));

    // When
    accountService.extendAccount(testMember, accountId, extensionDuration);

    // Then
    assertEquals(5000, testMember.getPoint()); // 변화 없음
    verify(memberRepository, never()).save(testMember);
    verify(pointLogRepository, never()).save(any(PointLog.class));
  }

  @Test
  @DisplayName("extendAccount - 존재하지 않는 계좌")
  void extendAccount_AccountNotFound() {
    // Given
    Long accountId = 999L;
    Integer extensionDuration = 3;

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> accountService.extendAccount(testMember, accountId, extensionDuration)
    );

    assertEquals("해당 적금을 찾을 수 없습니다.", exception.getMessage());
  }

  @Test
  @DisplayName("endAccount - 정상 종료 처리")
  void endAccount_Success() {
    // Given
    Long accountId = 1L;

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(memberRepository.save(any(Member.class))).thenReturn(testMember);
    when(pointLogRepository.save(any(PointLog.class))).thenReturn(new PointLog());
    when(routineRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

    // When
    accountService.endAccount(testMember, accountId);

    // Then
    assertTrue(testAccount.getIsEnded());
    assertEquals(6000, testMember.getPoint()); // 5000 + 1000(이자)

    // 연결된 루틴들이 종료되고 연결 해제되었는지 확인
    assertTrue(testRoutine1.getIsEnded());
    assertTrue(testRoutine2.getIsEnded());
    assertEquals(null, testRoutine1.getAccount());
    assertEquals(null, testRoutine2.getAccount());

    verify(memberRepository, times(1)).save(testMember);
    verify(pointLogRepository, times(1)).save(argThat(log ->
        log.getMember().equals(testMember) &&
            log.getPoint().equals(1000) &&
            log.getCategory().equals("적금 완료 이자 지급")
    ));
    verify(routineRepository, times(1)).saveAll(anyList());
    verify(accountRepository, times(1)).save(testAccount);
  }

  @Test
  @DisplayName("endAccount - 이미 종료된 적금")
  void endAccount_AlreadyEnded() {
    // Given
    Long accountId = 1L;
    testAccount.setIsEnded(true);

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));

    // When & Then
    BadRequestException exception = assertThrows(
        BadRequestException.class,
        () -> accountService.endAccount(testMember, accountId)
    );

    assertEquals("이미 종료된 적금입니다.", exception.getReason());
  }

  @Test
  @DisplayName("endAccount - 만료된 적금")
  void endAccount_ExpiredAccount() {
    // Given
    Long accountId = 1L;
    testAccount.setIsExpired(true);

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));

    // When & Then
    BadRequestException exception = assertThrows(
        BadRequestException.class,
        () -> accountService.endAccount(testMember, accountId)
    );

    assertEquals("만기된 적금은 종료할 수 없습니다.", exception.getReason());
  }

  @Test
  @DisplayName("endAccount - 루틴이 없는 경우")
  void endAccount_NoRoutines() {
    // Given
    Long accountId = 1L;
    testAccount.setRoutines(null);

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.of(testAccount));
    when(memberRepository.save(any(Member.class))).thenReturn(testMember);
    when(pointLogRepository.save(any(PointLog.class))).thenReturn(new PointLog());

    // When
    accountService.endAccount(testMember, accountId);

    // Then
    assertTrue(testAccount.getIsEnded());
    verify(routineRepository, never()).saveAll(anyList());
  }

  @Test
  @DisplayName("endAccount - 존재하지 않는 계좌")
  void endAccount_AccountNotFound() {
    // Given
    Long accountId = 999L;

    when(accountRepository.findByMemberAndId(testMember, accountId))
        .thenReturn(Optional.empty());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> accountService.endAccount(testMember, accountId)
    );

    assertEquals("해당 적금을 찾을 수 없습니다.", exception.getMessage());
  }
}
