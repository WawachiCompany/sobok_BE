package com.chihuahua.sobok.oauth;

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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OauthAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String oauthId;
  private String provider;

  @ManyToOne
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_oauth_account_member"))
  private Member member;

  @CreationTimestamp
  private LocalDateTime createdAt;
}
