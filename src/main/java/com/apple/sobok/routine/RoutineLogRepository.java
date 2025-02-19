package com.apple.sobok.routine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, Long> {
    Optional<RoutineLog> findByRoutineAndIsCompleted(Routine routine, Boolean isCompleted);

    List<RoutineLog> findByRoutineAndEndTimeBetween(Routine routine, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);

    Optional<RoutineLog> findAllByRoutineAndIsCompletedAndEndTimeBetween(Routine routine, Boolean isCompleted, LocalDateTime endTimeAfter, LocalDateTime endTimeBefore);
}
