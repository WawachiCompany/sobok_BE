package com.chihuahua.sobok.routine.todo;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  List<Todo> findByRoutine(Routine routine);

  @Query("SELECT t FROM Todo t JOIN t.routine.routineDays rd WHERE t.routine.member = :member AND rd.day = :day AND t.routine.isSuspended = false AND t.routine.isEnded = false AND t.routine.account.isExpired = false")
  List<Todo> findByMemberAndDay(@Param("member") Member member, @Param("day") String day);

  @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.routine.account.isExpired = false")
  List<Todo> findAllByMember(@Param("member") Member member);

  @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.startTime <= :startTime AND t.endTime >= :endTime")
  List<Todo> findAllByMemberAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
      @Param("member") Member member, @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);

  @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.routine.account.isExpired = false AND t.linkApp = :linkApp")
  List<Todo> findAllByMemberAndLinkApp(Member member, String linkApp);

  @Query("SELECT t FROM Todo t JOIN t.routine r JOIN r.routineDays rd " +
      "WHERE r.member = :member " +
      "AND rd.day IN :days " +
      "AND NOT (t.endTime <= :startTime OR t.startTime >= :endTime) " +
      "AND r.isSuspended = false " +
      "AND r.isEnded = false " +
      "AND r.account.isExpired = false")
  List<Todo> findOverlappingTodos(
      @Param("member") Member member,
      @Param("days") List<String> days,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);

  @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.id = :id")
  Optional<Todo> findByMemberAndId(Member member, Long id);

  // 루틴 삭제용
  @Modifying
  @Query("DELETE FROM Todo t WHERE t.routine.id = :routineId")
  void deleteByRoutineId(@Param("routineId") Long routineId);
}
