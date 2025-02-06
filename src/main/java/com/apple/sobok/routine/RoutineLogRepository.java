package com.apple.sobok.routine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, Long> {
    Optional<RoutineLog> findByRoutineAndIsCompleted(Routine routine, Boolean isCompleted);
}
