package com.apple.sobok.routine.todo;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoLogRepository extends JpaRepository<TodoLog, Long> {

    Optional<TodoLog> findByTodoAndIsCompleted(Todo todo, Boolean isCompleted);

    @Query("SELECT t FROM TodoLog t WHERE t.todo.routine.member = :member AND t.isCompleted = true AND t.endTime >= :startTime AND t.endTime < :endTime")
    List<TodoLog> findAllByMemberAndIsCompletedAndEndTimeBetWeen(@Param("member") Member member,
                                                                 @Param("startTime") LocalDateTime startTime,
                                                                 @Param("endTime") LocalDateTime endTime);

    List<TodoLog> findByTodoAndEndTimeBetween(Todo todo, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);

}
