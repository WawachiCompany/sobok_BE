package com.chihuahua.sobok.statistics.report;

import com.chihuahua.sobok.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SnowCard {

  @Id
  @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_snow_card_member"))
  private Member member;

  private String targetYearMonth;

  private String snowCard;
}
