package com.chihuahua.sobok.routine;


import com.chihuahua.sobok.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

  @Query(value = "SELECT r.* " +
      "FROM routine r " +
      "JOIN routine_day rd ON r.id = rd.routine_id " +
      "JOIN account a ON r.account_id = a.id " +
      "WHERE r.member_id = :userId AND rd.day = :day AND r.is_suspended = false AND r.is_ended = false AND a.is_expired = false", nativeQuery = true)
  List<Routine> findByUserIdAndDay(@Param("userId") Long id, @Param("day") String day);

  List<Routine> findByMember(Member member);

  List<Routine> findByMemberAndAccountIsExpired(Member member, Boolean isAccountExpired);

  Optional<Routine> findByMemberAndId(Member member, Long id);

  List<Routine> findByMemberAndIsEnded(Member member, Boolean isEnded);

  List<Routine> findByMemberAndIsSuspendedAndIsEndedAndAccountIsExpired(Member member,
      Boolean isSuspended, Boolean isEnded, Boolean isAccountExpired);

  @Modifying
  @Query("UPDATE Routine r SET r.isAchieved = false, r.isCompleted = false WHERE r.member.id = :memberId")
  void resetAchievedStatusByMemberId(@Param("memberId") Long memberId);

  @Query("SELECT COUNT(r) > 0 FROM Routine r WHERE r.member.id = :memberId AND r.id = :routineId")
  boolean existsByIdAndMemberId(@Param("memberId") Long memberId,
      @Param("routineId") Long routineId);


}
