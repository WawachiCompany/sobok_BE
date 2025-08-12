package com.chihuahua.sobok.routine;


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
public class RoutineLog {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "routine_id", foreignKey = @ForeignKey(name = "FK_routine_log_routine"))
  private Routine routine;

  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Long duration;

  private Boolean isCompleted;
}
