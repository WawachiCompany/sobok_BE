package com.chihuahua.sobok.routine.todo;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TodoCompletedEvent extends ApplicationEvent {

  private final Todo todo;

  public TodoCompletedEvent(Object source, Todo todo) {
    super(source);
    this.todo = todo;
  }

}
