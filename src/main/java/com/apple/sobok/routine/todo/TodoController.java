package com.apple.sobok.routine.todo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/todo")
public class TodoController {

    private final TodoService todoService;

    @PostMapping("/start")
    public void startTodo(@RequestParam Long todoId) {
        todoService.startTodo(todoId);
    }

    @PostMapping("/end")
    public void endTodo(@RequestParam Long todoLogId) {
        todoService.endTodo(todoLogId);
    }

    @GetMapping("/category")
    public ResponseEntity<?> getTodoCategory() {
        return todoService.getTodoCategory();
    }
}
