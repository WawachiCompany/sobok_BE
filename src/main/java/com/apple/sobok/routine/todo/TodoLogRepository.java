package com.apple.sobok.routine.todo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoLogRepository extends JpaRepository<TodoLog, Long> {

    Optional<TodoLog> findByTodoAndIsCompleted(Todo todo, Boolean isCompleted);
}
