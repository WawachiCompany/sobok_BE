package com.chihuahua.sobok.member.spareTIme;


import com.chihuahua.sobok.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SpareTime {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "FK_spare_time_member"))
  private Member member;

  private String title;
  private String startTime;
  private String endTime;
  private Long duration; // 단위: 분

  @OneToMany(mappedBy = "spareTime", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private List<SpareTimeDay> spareTimeDays = new ArrayList<>();

  // 헬퍼 메서드: day 추가
  public void addDay(String day) {
    SpareTimeDay spareTimeDay = new SpareTimeDay();
    spareTimeDay.setDay(day);
    spareTimeDay.setSpareTime(this);
    this.spareTimeDays.add(spareTimeDay);
  }

  // 헬퍼 메서드: day 리스트 설정
  public void setDays(List<String> days) {
    this.spareTimeDays.clear();
    if (days != null) {
      for (String day : days) {
        addDay(day);
      }
    }
  }

  // 헬퍼 메서드: day 리스트 조회
  public List<String> getDays() {
    return this.spareTimeDays.stream()
        .map(SpareTimeDay::getDay)
        .toList();
  }
}
