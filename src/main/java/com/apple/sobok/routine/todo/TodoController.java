package com.apple.sobok.routine.todo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.http.ResponseEntity.ok;

@Controller
@RequiredArgsConstructor
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;

    @PostMapping("/start")
    public ResponseEntity<?> startTodo(@RequestParam Long todoId) {
        return todoService.startTodo(todoId);
    }

    @PostMapping("/end")
    public ResponseEntity<?> endTodo(@RequestParam Long todoLogId, @RequestParam Long duration) {
        return todoService.endTodo(todoLogId, duration);
    }

    @GetMapping("/category")
    public ResponseEntity<?> getTodoCategory() {
        return todoService.getTodoCategory();
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayTodos() {
        return todoService.getTodayTodos();
    }

    @GetMapping("/closest")
    public ResponseEntity<?> getClosestTodo() {
        return todoService.getClosestTodo();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTodos() { return todoService.getAllTodos(); }
}
