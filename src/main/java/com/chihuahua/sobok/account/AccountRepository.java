package com.chihuahua.sobok.account;

import com.chihuahua.sobok.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

  List<Account> findByMemberAndIsExpired(Member member, Boolean isExpired);

  List<Account> findByMemberAndIsExpiredAndIsEnded(Member member, Boolean isExpired,
      Boolean isEnded);

  Optional<Account> findByMemberAndId(Member member, Long id);

  List<Account> findByMemberAndIsValidAndIsExpired(Member member, Boolean isValid,
      Boolean isExpired);

  List<Account> findByIsExpired(Boolean isExpired);

  List<Account> findByMember(Member member);

  // 루틴 삭제용
  @Modifying
  @Query("UPDATE Account a SET a.routines = CASE WHEN a.id = :accountId THEN (SELECT r FROM a.routines r WHERE r.id != :routineId) ELSE a.routines END WHERE a.id = :accountId")
  void detachRoutineFromAccount(@Param("routineId") Long routineId,
      @Param("accountId") Long accountId);

}
