package com.chihuahua.sobok.member.point;


import com.chihuahua.sobok.member.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
public class PointLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_point_log_member"))
  private Member member;


  private Integer point;
  private Integer balance;
  private String category;
  private String description; // 내역 상세 있으면 넣고 없으면 빼기

  @CreationTimestamp
  private LocalDateTime createdAt;
}
