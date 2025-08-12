package com.chihuahua.sobok.statistics;

import com.chihuahua.sobok.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DailyAchieve {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_daily_achieve_member"))
  private Member member;

  private LocalDate date;
  private String status; // 달성 여부(All_ACHEIVED, SOME_ACHIEVED, NONE_ACHIEVED, NO_ROUTINE)
}
