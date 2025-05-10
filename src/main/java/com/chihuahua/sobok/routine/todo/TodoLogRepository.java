package com.chihuahua.sobok.routine.todo;

import com.chihuahua.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoLogRepository extends JpaRepository<TodoLog, Long> {


    @Query("SELECT t FROM TodoLog t WHERE t.todo.routine.member = :member AND t.isCompleted = true AND t.endTime >= :startTime AND t.endTime < :endTime")
    List<TodoLog> findAllByMemberAndIsCompletedAndEndTimeBetWeen(@Param("member") Member member,
                                                                 @Param("startTime") LocalDateTime startTime,
                                                                 @Param("endTime") LocalDateTime endTime);

    List<TodoLog> findByTodoAndEndTimeBetween(Todo todo, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);

    @Query("SELECT t FROM TodoLog t WHERE t.todo.routine.member = :member AND t.isCompleted = true")
    List<TodoLog> findAllByMemberAndIsCompleted(@Param("member") Member member);

    void deleteTodoLogByTodo(Todo todo);

    // 루틴 삭제용
    @Modifying
    @Query("DELETE FROM TodoLog tl WHERE tl.todo.routine.id = :routineId")
    void deleteByRoutineId(@Param("routineId") Long routineId);


}
