package com.apple.sobok.account;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.routine.RoutineDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final AccountService accountService;
    private final AccountLogRepository accountLogRepository;


    @GetMapping("/account/new")
    public String account() {
        return "account.html";
    }

    @PostMapping("/account/create")
    public ResponseEntity<?> createAccount(@RequestBody AccountDto accountDto) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            Account account = new Account();
            account.setMember(member);
            account.setTitle(accountDto.getTitle());
            account.setTarget(accountDto.getTarget());
            account.setIsPublic(accountDto.getIsPublic());
            account.setTime(accountDto.getTime());
            account.setDuration(accountDto.getDuration());
            account.setBalance(0);
            account.setIsExpired(false);
            account.setCreatedAt(LocalDate.now());
            account.setIsValid(false);
            account.setInterest(3.14f); // 이율 계산 필요
            account.setExpiredAt(LocalDate.now().plusMonths(accountDto.getDuration()));
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

            Map<String, Object> response = new HashMap<>();
            response.put("account_id", account.getId());
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

    @GetMapping("/account/valid-list")
    public ResponseEntity<?> getValidAccountList() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

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

    @GetMapping("/account/expired-list")
    public ResponseEntity<?> getExpiredAccountList() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

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

    @GetMapping("/account/details")
    public ResponseEntity<?> getAccountDetails(@RequestParam Long accountId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            Optional<Account> result = accountRepository.findByMemberAndId(member, accountId);
            if (result.isEmpty()) {
                throw new IllegalArgumentException("적금을 찾을 수 없습니다.");
            }
            Account account = result.get();
            Map<String, Object> response = new HashMap<>();
            response.put("title", account.getTitle());    // 적금 제목
            response.put("target", account.getTarget());    // 적금 목표 금액
            response.put("balance", account.getBalance());    // 현재 잔액
            response.put("time", account.getTime());    // 저금 시간
            response.put("duration", account.getDuration());    // 적금 기간
            response.put("is_expired", account.getIsExpired());    // 만기 여부
            response.put("is_valid", account.getIsValid());    // 활성화 여부
            response.put("interest", account.getInterest());    // 이율
            response.put("expired_at", account.getExpiredAt());    // 만기일
            response.put("routines", account.getRoutines().stream().map(routine -> {
                RoutineDto routineDto = new RoutineDto();
                routineDto.setTitle(routine.getTitle());
                return routineDto;
            }).collect(Collectors.toList()));    // 루틴 목록(제목만 가져옴)
            response.put("message", "적금 상세 조회 성공");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/account/delete")
    public ResponseEntity<?> deleteAccount(@RequestParam Long accountId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            Optional<Account> result = accountRepository.findByMemberAndId(member, accountId);
            if (result.isEmpty()) {
                throw new IllegalArgumentException("적금을 찾을 수 없습니다.");
            }
            Account account = result.get();
            accountRepository.delete(account);

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

    @PutMapping("/account/edit")
    public ResponseEntity<?> editAccount(@RequestBody AccountDto accountDto, @RequestParam Long accountId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println(username);
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            Optional<Account> result = accountRepository.findByMemberAndId(member, accountId);
            if (result.isEmpty()) {
                throw new IllegalArgumentException("적금을 찾을 수 없습니다.");
            }

            Account account = result.get();
            if (accountDto.getTitle() != null) {
                account.setTitle(accountDto.getTitle());
            }
            if (accountDto.getTarget() != null) {
                account.setTarget(accountDto.getTarget());
            }
            if (accountDto.getIsPublic() != null) {
                account.setIsPublic(accountDto.getIsPublic());
            }
            if (accountDto.getTime() != null) {
                account.setTime(accountDto.getTime());
            }
            if (accountDto.getInterest() != null) {
                account.setInterest(accountDto.getInterest());
            }
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);

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

    @PostMapping("/account/deposit")
    public ResponseEntity<?> depositAccount(@RequestParam Long accountId, @RequestParam Integer amount) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            Optional<Account> result = accountRepository.findByMemberAndId(member, accountId);
            if (result.isEmpty()) {
                throw new IllegalArgumentException("적금을 찾을 수 없습니다.");
            }
            Account account = result.get();
            account.setBalance(account.getBalance() + amount);
            accountRepository.save(account);

            // 로그 생성
            AccountLog accountLog = new AccountLog();
            accountLog.setAccount(account);
            accountLog.setDepositTime(amount);
            accountLog.setCreatedAt(LocalDateTime.now());
            accountLogRepository.save(accountLog);

            Map<String, Object> response = new HashMap<>();
            response.put("account_id", account.getId());
            response.put("balance", account.getBalance());
            response.put("message", "적금 입금 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "적금 입금 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



}


