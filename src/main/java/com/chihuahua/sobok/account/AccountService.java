package com.chihuahua.sobok.account;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.member.MemberService;
import com.chihuahua.sobok.member.point.PointLog;
import com.chihuahua.sobok.member.point.PointLogRepository;
import com.chihuahua.sobok.routine.Routine;
import com.chihuahua.sobok.routine.RoutineAccountDto;
import com.chihuahua.sobok.routine.RoutineRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountLogRepository accountLogRepository;
    private final PointLogRepository pointLogRepository;
    private final MemberRepository memberRepository;
    private final RoutineRepository routineRepository;
    private final MemberService memberService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional
    public void createAccount(AccountDto accountDto, Member member) {
        member = entityManager.merge(member);
        Account account = new Account();
        account.setTitle(accountDto.getTitle());
        account.setTime(accountDto.getTime());
        account.setDuration(accountDto.getDuration());
        account.setInterest(calculateInitialInterest(account)); // 이율 계산 필요
        account.setExpiredAt(LocalDate.now().plusMonths(accountDto.getDuration()));

        // 멤버와 적금 연결(헬퍼 메서드)
        member.addAccount(account);

        accountRepository.save(account);



        if (accountDto.getRoutineIds() != null) {
            // 루틴 연결
            List<Long> routineIds = accountDto.getRoutineIds().stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList());
            List<Routine> routines = routineRepository.findAllById(routineIds);
            account.setRoutines(routines);
            for (Routine routine : routines) {
                routine.setAccount(account);
                routineRepository.save(routine);
            }
        }

    }

    public List<AccountDto> getAccountList(List<Account> result) {
        return result.stream().map(account -> {
            AccountDto dto = new AccountDto();
            dto.setId(account.getId());
            dto.setTitle(account.getTitle());
            dto.setTime(account.getTime());
            dto.setDuration(account.getDuration());
            dto.setIsValid(account.getIsValid());
            dto.setInterest(account.getInterest());
            dto.setExpiredAt(account.getExpiredAt());
            return dto;
        }).collect(Collectors.toList());

    }

    public Map<String, Object> getAccountDetails(Member member, Long accountId) {
        Account account = accountRepository.findByMemberAndId(member, accountId);
        if (account == null) {
            throw new IllegalArgumentException("해당 적금을 찾을 수 없습니다.");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("title", account.getTitle());    // 적금 제목
        response.put("balance", account.getBalance());    // 현재 잔액
        response.put("time", account.getTime());    // 저금 시간
        response.put("duration", account.getDuration());    // 적금 기간
        response.put("is_expired", account.getIsExpired());    // 만기 여부
        response.put("is_valid", account.getIsValid());    // 활성화 여부
        response.put("interest", account.getInterest());    // 이율
        response.put("created_at", account.getCreatedAt());    // 생성일
        response.put("expired_at", account.getExpiredAt());    // 만기일
        response.put("routines", account.getRoutines().stream().map(routine -> {
            RoutineAccountDto routineAccountDto = new RoutineAccountDto();
            routineAccountDto.setTitle(routine.getTitle());
            routineAccountDto.setId(routine.getId());
            routineAccountDto.setStartTime(routine.getStartTime());
            routineAccountDto.setEndTime(routine.getEndTime());
            routineAccountDto.setDuration(routine.getDuration());
            return routineAccountDto;
        }).collect(Collectors.toList()));    // 루틴 목록(제목만 가져옴)
        response.put("message", "적금 상세 조회 성공");
        return response;
    }

    @Transactional
    public void deleteAccount(Long accountId) {

        Member member = memberService.getMember();

        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("해당 적금을 찾을 수 없습니다."));

        // 적금 시작일과 만기일을 전체로 봤을 때 적금을 삭제하는 시점까지의 비율 계산
        LocalDate startDate = account.getCreatedAt();
        LocalDate endDate = account.getExpiredAt();
        LocalDate now = LocalDate.now();
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        long daysPassed = java.time.temporal.ChronoUnit.DAYS.between(startDate, now);
        float ratio = (float) daysPassed / totalDays;

        // 이자 지급
        member.setPoint(member.getPoint() + Math.round(account.getInterestBalance() * ratio));
        memberRepository.save(member);

        if(Math.round(account.getInterestBalance() * ratio) != 0) {
            PointLog pointLog = new PointLog();
            pointLog.setMember(member);
            pointLog.setPoint(Math.round(account.getInterestBalance() * ratio));
            pointLog.setBalance(member.getPoint() + pointLog.getPoint());
            pointLog.setCategory("적금 중도 해지 이자 지급");
            pointLogRepository.save(pointLog);
        }

        // 적금에 연결된 루틴 모두 종료 처리 및 적금 연결 해제
        List<Routine> routines = account.getRoutines();
        if(!routines.isEmpty()) {
            for (Routine routine : routines) {
                routine.setIsEnded(true);
                routine.setAccount(null);
            }
            routineRepository.saveAll(routines);
        }


        // 적금 로그 삭제
        accountLogRepository.deleteAllByAccount(account);

        // 멤버 적금 연결관계 해제(헬퍼 메서드)
        member.removeAccount(account);


        // 적금 삭제
        accountRepository.delete(account);
    }

    public Account updateAccount(Member member, Long accountId, AccountDto accountDto) {
        Account account = accountRepository.findByMemberAndId(member, accountId);
        if (accountDto.getTitle() != null) {
            account.setTitle(accountDto.getTitle());
        }
        if (accountDto.getTime() != null) {
            account.setTime(accountDto.getTime());
        }

        account.setInterest(calculateInitialInterest(account)); // 이율 계산 필요
        accountRepository.save(account);
        return account;
    }

    public ResponseEntity<?> depositAccount(Member member, Long accountId, Integer amount) {
        Account account = accountRepository.findByMemberAndId(member, accountId);
        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        // 로그 생성
        AccountLog accountLog = new AccountLog();
        accountLog.setAccount(account);
        accountLog.setDepositTime(amount);
        accountLog.setBalance(account.getBalance());
        accountLogRepository.save(accountLog);

        // totalAchievedTime 업데이트
        // totalAccountBalance 업데이트
        int totalAccountBalance = member.getAccounts().stream()
                .mapToInt(Account::getBalance)
                .sum();
        member.setTotalAccountBalance(totalAccountBalance);
        member.setTotalAchievedTime(member.getTotalAchievedTime() + amount);
        memberRepository.save(member);

        Map<String, Object> response = new HashMap<>();
        response.put("account_id", account.getId());
        response.put("balance", account.getBalance());
        response.put("message", "적금 입금 완료");
        return ResponseEntity.ok(response);
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

    public float calculateInitialInterest(Account account) {
        Integer time = account.getTime();
        if(time < 600) {
            return 3f;
        }
        else if(time < 1200) {
            return 4f;
        }
        else if(time < 2400) {
            return 5f;
        }
        else {
            return 7f;
        }
    }

    public ResponseEntity<?> getAccountLog(Account account, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> response = new HashMap<>();
        List<AccountLog> accountLogs = accountLogRepository.findByAccountAndCreatedAtBetween(account, start, end);

        // AccountLogDto로 변환(account 데이터 제거)
        List<AccountLogDto> accountLogDtos = accountLogs.stream().map(log -> {
            AccountLogDto dto = new AccountLogDto();
            dto.setId(log.getId());
            dto.setDepositTime(log.getDepositTime());
            dto.setBalance(log.getBalance());
            dto.setCreatedAt(log.getCreatedAt());
            return dto;
        }).toList();

        response.put("account_logs", accountLogDtos);
        response.put("message", "적금 로그 조회 성공");
        return ResponseEntity.ok(response);
    }

    public void extendAccount(Account account, Integer duration) {
        Member member = account.getMember();

        if(account.getInterestBalance() != 0) {
            // 이자 지급
            member.setPoint(member.getPoint() + account.getInterestBalance());
            memberRepository.save(member);

            // 포인트 로그 생성
            PointLog pointLog = new PointLog();
            pointLog.setMember(member);
            pointLog.setPoint(account.getInterestBalance());
            pointLog.setBalance(member.getPoint() + pointLog.getPoint());
            pointLog.setCategory("적금 연장 전 이자 지급");
            pointLogRepository.save(pointLog);
        }
        account.setExpiredAt(account.getExpiredAt().plusMonths(duration));
        account.setIsExpired(false);
        accountRepository.save(account);
    }

    public void endAccount(Account account) {
        Member member = account.getMember();

        if(account.getInterestBalance() != 0) {
            // 이자 지급
            member.setPoint(member.getPoint() + account.getInterestBalance());
            memberRepository.save(member);

            // 포인트 로그 생성
            PointLog pointLog = new PointLog();
            pointLog.setMember(member);
            pointLog.setPoint(account.getInterestBalance());
            pointLog.setBalance(member.getPoint() + pointLog.getPoint());
            pointLog.setCategory("적금 완료 이자 지급");
            
            pointLogRepository.save(pointLog);
        }

        // 적금에 연결된 루틴 모두 종료 처리 및 적금 연결 해제
        List<Routine> routines = account.getRoutines();
        if(!routines.isEmpty()) {
            for (Routine routine : routines) {
                routine.setIsEnded(true);
                routine.setAccount(null);
            }
            routineRepository.saveAll(routines);
        }

        account.setIsEnded(true);
    }

}
