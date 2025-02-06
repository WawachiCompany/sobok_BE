package com.apple.sobok.account;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final MemberService memberService;


    @GetMapping("/new")
    public String account() {
        return "account.html";
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody AccountDto accountDto) {
        try {
            Member member = memberService.getMember();

            accountService.createAccount(accountDto, member);

            // 적금 생성 후 구독권 가격 계산
            member.setPremiumPrice(memberService.calculatePremiumPrice(member));
            memberRepository.save(member);

            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 생성 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 생성 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }

    @GetMapping("/list/ongoing")
    public ResponseEntity<?> getOngoingAccountList() {
        try {
            Member member = memberService.getMember();

            List<Account> result = accountRepository.findByMemberAndIsExpired(member, false);
            List<AccountDto> accountList = accountService.getAccountList(result);

            Map<String, Object> response = new HashMap<>();
            response.put("account_list", accountList);
            response.put("message", "진행 중인 적금 조회 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "진행 중인 적금 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/list/expired")
    public ResponseEntity<?> getExpiredAccountList() {
        try {
            Member member = memberService.getMember();

            List<Account> result = accountRepository.findByMemberAndIsExpired(member, true);
            List<AccountDto> accountList = accountService.getAccountList(result);

            Map<String, Object> response = new HashMap<>();
            response.put("account_list", accountList);
            response.put("message", "만기된 적금 조회 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "만기된 적금 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/list/valid")
    public ResponseEntity<?> getValidAccountList() {
        try {
            Member member = memberService.getMember();

            List<Account> result = accountRepository.findByMemberAndIsValidAndIsExpired(member, true, false);

            if(result.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "활성화된 적금이 없습니다.");
                return ResponseEntity.ok(response);
            }
            List<AccountDto> accountList = accountService.getAccountList(result);
            Map<String, Object> response = new HashMap<>();
            response.put("account_list", accountList);
            response.put("message", "활성화된 적금 조회 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "활성화된 적금 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/list/invalid")
    public ResponseEntity<?> getInvalidAccountList() {
        try {
            Member member = memberService.getMember();

            List<Account> result = accountRepository.findByMemberAndIsValidAndIsExpired(member, false, false);
            if(result.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "비활성화된 적금이 없습니다.");
                return ResponseEntity.ok(response);
            }
            List<AccountDto> accountList = accountService.getAccountList(result);
            Map<String, Object> response = new HashMap<>();
            response.put("account_list", accountList);
            response.put("message", "비활성화된 적금 조회 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "비활성화된 적금 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getAccountDetails(@RequestParam Long accountId) {
        try {
            Member member = memberService.getMember();

            Map<String, Object> response = accountService.getAccountDetails(member, accountId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestParam Long accountId) {
        try {

           accountService.deleteAccount(accountId);

            Member member = memberService.getMember();

            // 적금 삭제 후 구독권 가격 계산
            member.setPremiumPrice(memberService.calculatePremiumPrice(member));
            memberRepository.save(member);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "적금 삭제 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editAccount(@RequestBody AccountDto accountDto, @RequestParam Long accountId) {
        try {
            Member member = memberService.getMember();

            Account account = accountService.updateAccount(member, accountId, accountDto);

            // 적금 수정 후 구독권 가격 계산
            member.setPremiumPrice(memberService.calculatePremiumPrice(member));
            memberRepository.save(member);

            Map<String, Object> response = new HashMap<>();
            response.put("account_id", account.getId());
            response.put("timestamp", account.getUpdatedAt());
            response.put("message", "적금 수정 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 수정 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> depositAccount(@RequestParam Long accountId, @RequestParam Integer amount) {
        try {
            Member member = memberService.getMember();
            return accountService.depositAccount(member, accountId, amount);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 입금 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}


