package com.apple.sobok.account;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import com.apple.sobok.routine.Routine;
import com.apple.sobok.routine.RoutineDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountLogRepository accountLogRepository;

    public void createAccount(AccountDto accountDto, Member member) {
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
        account.setInterestBalance(0L);
        account.setExpiredAt(LocalDate.now().plusMonths(accountDto.getDuration()));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    public List<AccountDto> getAccountList(List<Account> result) {
        return result.stream().map(account -> {
            AccountDto dto = new AccountDto();
            dto.setId(account.getId());
            dto.setTitle(account.getTitle());
            dto.setTarget(account.getTarget());
            dto.setIsPublic(account.getIsPublic());
            dto.setTime(account.getTime());
            dto.setDuration(account.getDuration());
            dto.setIsValid(account.getIsValid());
            dto.setInterest(account.getInterest());
            return dto;
        }).collect(Collectors.toList());

    }

    public Map<String, Object> getAccountDetails(Member member, Long accountId) {
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
        return response;
    }

    public void deleteAccount(Member member, Long accountId) {
        Optional<Account> result = accountRepository.findByMemberAndId(member, accountId);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("적금을 찾을 수 없습니다.");
        }
        Account account = result.get();
        accountRepository.delete(account);
    }

    public Account updateAccount(Member member, Long accountId, AccountDto accountDto) {
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
        return account;
    }

    public Map<String, Object> depositAccount(Member member, Long accountId, Integer amount) {
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
        accountLog.setCreatedAt(LocalDateTime.now().minusMonths(1));
        accountLogRepository.save(accountLog);

        Map<String, Object> response = new HashMap<>();
        response.put("account_id", account.getId());
        response.put("balance", account.getBalance());
        response.put("message", "적금 입금 완료");
        return response;
    }

    public void validateAccount(Account account) {
        //Account 활성화 여부 체크(루틴의 duration 합 = 적금의 duration)
        List<Routine> routines = account.getRoutines();
        long totalDurationOfRoutines = routines.stream()
                .mapToLong(routine -> routine.getDuration() * routine.getDays().size())
                .sum() * 4; // 4주로 고정
        if (totalDurationOfRoutines == account.getTime()) {
            account.setIsValid(true);
            accountRepository.save(account);
        }
        else {
            account.setIsValid(false);
            accountRepository.save(account);
        }
    }

}
