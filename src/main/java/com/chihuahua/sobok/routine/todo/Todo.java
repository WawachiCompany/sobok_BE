package com.chihuahua.sobok.routine.todo;

import com.chihuahua.sobok.routine.Routine;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Todo {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "routine_id", foreignKey = @ForeignKey(name = "FK_todo_routine"))
  private Routine routine;

  private String title;
  private String category;
  private LocalTime startTime;
  private LocalTime endTime;
  private Long duration;
  private String linkApp;
  private Boolean isCompleted = false; // 90% 이상 넘겨야 완료됨

  @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  List<TodoLog> todoLogs = new ArrayList<>();

  // Todo에 Routine 설정
  public void setRoutine(Routine routine) {
    if (this.routine != null) {
      this.routine.getTodos().remove(this); // 기존 Routine에서 제거
    }
    this.routine = routine;
    if (routine != null && !routine.getTodos().contains(this)) {
      routine.getTodos().add(this); // 새로운 Routine에 추가
    }
  }

  // Todo에 TodoLog 추가
  public void setTodoLog(TodoLog todoLog) {
    todoLogs.add(todoLog);
    todoLog.setTodo(this);
  }

  // Todo에 TodoLog 제거
  public void removeTodoLog(TodoLog todoLog) {
    todoLogs.remove(todoLog);
    todoLog.setTodo(null);
  }

}
