package com.apple.sobok.member.spareTIme;


import com.apple.sobok.member.Member;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import java.util.List;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Getter
@Setter
public class SpareTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String title;
    private String startTime;
    private String endTime;
    private Long duration; // 단위: 분

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "spare_time_days", joinColumns = @JoinColumn(name = "spare_time_id"))
    @Cascade(CascadeType.ALL)
    private List<String> days; // 요일 리스트(월 ~ 일)

}
