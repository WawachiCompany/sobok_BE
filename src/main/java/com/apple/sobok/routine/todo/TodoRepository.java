package com.apple.sobok.routine.todo;

import com.apple.sobok.member.Member;
import com.apple.sobok.routine.Routine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByRoutine(Routine routine);
    @Query("SELECT t FROM Todo t WHERE t.routine.member = :member AND :day MEMBER OF t.routine.days AND t.routine.isSuspended = false AND t.routine.isEnded = false")
    List<Todo> findByMemberAndDay(@Param("member") Member member, @Param("day") String day);

    @Query("SELECT t FROM Todo t WHERE t.routine.member = :member")
    List<Todo> findAllByMember(@Param("member") Member member);

}
