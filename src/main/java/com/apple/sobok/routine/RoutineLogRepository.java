package com.apple.sobok.routine;

import com.apple.sobok.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, Long> {
    Optional<RoutineLog> findByRoutineAndIsCompleted(Routine routine, Boolean isCompleted);

    List<RoutineLog> findByRoutineAndEndTimeBetween(Routine routine, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);

    Optional<RoutineLog> findByRoutineAndIsCompletedAndEndTimeBetween(Routine routine, Boolean isCompleted, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);

    List<RoutineLog> findAllByRoutineAndIsCompletedAndEndTimeBetween(Routine routine, Boolean isCompleted, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);


    @Query("select rl from RoutineLog rl where rl.routine.member = :member and rl.isCompleted = true and rl.endTime between :endTimeAfter and :endTimeBefore")
    List<RoutineLog> findAllByMemberAndIsCompletedAndEndTimeBetween(@Param("member") Member member, @Param("endTimeAfter") LocalDateTime endTimeAfter, @Param("endTimeBefore") LocalDateTime endTimeBefore);
}
