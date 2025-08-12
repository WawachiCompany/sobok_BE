package com.chihuahua.sobok.routine.todo;


import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class TodoLog {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "todo_id", foreignKey = @ForeignKey(name = "FK_todo_log_todo"))
  private Todo todo;

  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Long duration;

  private Boolean isCompleted;

  @ManyToOne
  @JoinColumn(name = "routine_log_id", foreignKey = @ForeignKey(name = "FK_todo_log_routine_log"))
  private com.chihuahua.sobok.routine.RoutineLog routineLog;
}
