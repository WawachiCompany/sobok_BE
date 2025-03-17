package com.chihuahua.sobok.routine.todo;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.routine.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByRoutine(Routine routine);
    @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND :day MEMBER OF t.routine.days AND t.routine.isSuspended = false AND t.routine.isEnded = false AND t.routine.account.isExpired = false")
    List<Todo> findByMemberAndDay(@Param("member") Member member, @Param("day") String day);

    @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.routine.account.isExpired = false")
    List<Todo> findAllByMember(@Param("member") Member member);

    @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.startTime <= :startTime AND t.endTime >= :endTime")
    List<Todo> findAllByMemberAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(@Param("member") Member member, @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);

    @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND t.routine.account.isExpired = false AND t.linkApp = :linkApp")
    List<Todo> findAllByMemberAndLinkApp(Member member, String linkApp);
}
