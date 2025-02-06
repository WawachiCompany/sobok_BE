package com.apple.sobok.routine.todo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoLogRepository extends JpaRepository<TodoLog, Long> {
    List<TodoLog> findByTodoAndIsCompleted(Todo todo, Boolean isCompleted);
}
