package com.chihuahua.sobok.survey;


import com.chihuahua.sobok.member.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Survey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "member_id")
  private Member member;

  private String spareTpo;

  @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private List<SurveySpareTime> surveySpareTimeList = new ArrayList<>();

  private String preference1;
  private String preference2;
  private String preference3;

  @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  private List<SurveyLikeOption> surveyLikeOptionList = new ArrayList<>();

  private String extraRequest;

  // 헬퍼 메서드: spareTime 추가
  public void addSpareTime(String spareTime) {
    SurveySpareTime surveySpareTime = new SurveySpareTime();
    surveySpareTime.setSpareTime(spareTime);
    surveySpareTime.setSurvey(this);
    this.surveySpareTimeList.add(surveySpareTime);
  }

  // 헬퍼 메서드: spareTime 리스트 설정
  public void setSpareTime(List<String> spareTimeList) {
    this.surveySpareTimeList.clear();
    if (spareTimeList != null) {
      for (String spareTime : spareTimeList) {
        addSpareTime(spareTime);
      }
    }
  }

  // 헬퍼 메서드: spareTime 리스트 조회
  public List<String> getSpareTime() {
    return this.surveySpareTimeList.stream()
        .map(SurveySpareTime::getSpareTime)
        .toList();
  }

  // 헬퍼 메서드: likeOption 추가
  public void addLikeOption(String likeOption) {
    SurveyLikeOption surveyLikeOption = new SurveyLikeOption();
    surveyLikeOption.setLike_option(likeOption);
    surveyLikeOption.setSurvey(this);
    this.surveyLikeOptionList.add(surveyLikeOption);
  }

  // 헬퍼 메서드: likeOption 리스트 설정
  public void setLikeOption(List<String> likeOptionList) {
    this.surveyLikeOptionList.clear();
    if (likeOptionList != null) {
      for (String likeOption : likeOptionList) {
        addLikeOption(likeOption);
      }
    }
  }

  // 헬퍼 메서드: likeOption 리스트 조회
  public List<String> getLikeOption() {
    return this.surveyLikeOptionList.stream()
        .map(SurveyLikeOption::getLike_option)
        .toList();
  }
}
