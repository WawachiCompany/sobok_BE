package com.chihuahua.sobok.account;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.member.MemberService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

  private final AccountRepository accountRepository;
  private final MemberRepository memberRepository;
  private final AccountService accountService;
  private final MemberService memberService;


  @PostMapping("/create")
  public ResponseEntity<?> createAccount(@RequestBody AccountDto accountDto) {
    Member member = memberService.getMember();

    accountService.createAccount(accountDto, member);

    // 적금 생성 후 구독권 가격 계산
    member.setPremiumPrice(memberService.calculatePremiumPrice(member));
    memberRepository.save(member);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "적금 생성 완료");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list/ongoing")
  public ResponseEntity<?> getOngoingAccountList() {
    Member member = memberService.getMember();

    List<Account> result = accountRepository.findByMemberAndIsExpired(member, false);
    List<AccountDto> accountList = accountService.getAccountList(result);

    Map<String, Object> response = new HashMap<>();
    response.put("account_list", accountList);
    response.put("message", "진행 중인 적금 조회 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list/expired")
  public ResponseEntity<?> getExpiredAccountList() {
    Member member = memberService.getMember();

    List<Account> result = accountRepository.findByMemberAndIsExpiredAndIsEnded(member, true,
        false);
    List<AccountDto> accountList = accountService.getAccountList(result);

    Map<String, Object> response = new HashMap<>();
    response.put("account_list", accountList);
    response.put("message", "만기된 적금 조회 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list/ended")
  public ResponseEntity<?> getEndedAccountList() {
    Member member = memberService.getMember();

    List<Account> result = accountRepository.findByMemberAndIsExpiredAndIsEnded(member, true, true);
    List<AccountDto> accountList = accountService.getAccountList(result);

    Map<String, Object> response = new HashMap<>();
    response.put("account_list", accountList);
    response.put("message", "완료된 적금 조회 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list/valid")
  public ResponseEntity<?> getValidAccountList() {
    Member member = memberService.getMember();

    List<Account> result = accountRepository.findByMemberAndIsValidAndIsExpired(member, true,
        false);

    if (result.isEmpty()) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "활성화된 적금이 없습니다.");
      return ResponseEntity.ok(response);
    }
    List<AccountDto> accountList = accountService.getAccountList(result);
    Map<String, Object> response = new HashMap<>();
    response.put("account_list", accountList);
    response.put("message", "활성화된 적금 조회 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/list/invalid")
  public ResponseEntity<?> getInvalidAccountList() {
    Member member = memberService.getMember();

    List<Account> result = accountRepository.findByMemberAndIsValidAndIsExpired(member, false,
        false);
    if (result.isEmpty()) {
      Map<String, Object> response = new HashMap<>();
      response.put("message", "비활성화된 적금이 없습니다.");
      return ResponseEntity.ok(response);
    }
    List<AccountDto> accountList = accountService.getAccountList(result);

    Map<String, Object> response = new HashMap<>();
    response.put("account_list", accountList);
    response.put("message", "비활성화된 적금 조회 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/details")
  public ResponseEntity<?> getAccountDetails(@RequestParam Long accountId) {
    Member member = memberService.getMember();

    Map<String, Object> response = accountService.getAccountDetails(member, accountId);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteAccount(@RequestParam Long accountId) {
    Member member = memberService.getMember();
    accountService.deleteAccount(member, accountId);
    // 적금 삭제 후 구독권 가격 계산
    member.setPremiumPrice(memberService.calculatePremiumPrice(member));
    memberRepository.save(member);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "적금 삭제 성공");
    return ResponseEntity.ok(response);
  }

  @PutMapping("/edit")
  public ResponseEntity<?> editAccount(@RequestBody AccountDto accountDto,
      @RequestParam Long accountId) {
    Member member = memberService.getMember();

    Account account = accountService.updateAccount(member, accountId, accountDto);

    // 적금 수정 후 구독권 가격 계산
    member.setPremiumPrice(memberService.calculatePremiumPrice(member));
    memberRepository.save(member);

    Map<String, Object> response = new HashMap<>();
    response.put("account_id", account.getId());
    response.put("message", "적금 수정 완료");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/deposit")
  public ResponseEntity<?> depositAccount(@RequestParam Long accountId,
      @RequestParam Integer amount) {
    Member member = memberService.getMember();
    Account account = accountService.depositAccount(member, accountId, amount);
    Map<String, Object> response = new HashMap<>();
    response.put("account_id", account.getId());
    response.put("balance", account.getBalance());
    response.put("message", "적금 입금 완료");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/log")
  public ResponseEntity<?> getAccountLog(@RequestParam Long accountId,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    Member member = memberService.getMember();
    List<AccountLogDto> logs = accountService.getAccountLog(member, accountId, startDate, endDate);
    Map<String, Object> response = new HashMap<>();
    response.put("account_logs", logs);
    response.put("message", "적금 로그 조회 성공");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/extend")
  public ResponseEntity<?> extendAccount(@RequestParam Long accountId,
      @RequestParam Integer duration) {
    Member member = memberService.getMember();
    accountService.extendAccount(member, accountId, duration);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "적금 연장 완료");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/end")
  public ResponseEntity<?> endAccount(@RequestParam Long accountId) {
    Member member = memberService.getMember();
    accountService.endAccount(member, accountId);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "적금 종료 완료");
    return ResponseEntity.ok(response);
  }

}


