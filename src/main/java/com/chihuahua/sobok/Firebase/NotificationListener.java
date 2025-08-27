package com.chihuahua.sobok.Firebase;

import com.chihuahua.sobok.routine.todo.Todo;
import com.chihuahua.sobok.routine.todo.TodoCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

  private final FirebaseService firebaseService;

  @EventListener
  public void handleTodoCompletedEvent(TodoCompletedEvent event) {
    Todo completedTodo = event.getTodo();
    firebaseService.sendPushNotification("축하합니다!",
        "할 일을 완료했어요: " + completedTodo.getTitle(),
        completedTodo.getRoutine().getMember());
  }

}
