package com.apple.sobok.routine.todo;

import com.apple.sobok.routine.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByRoutine(Routine routine);
}
