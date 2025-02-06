package com.apple.sobok.survey;


import com.apple.sobok.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.List;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "survey_spare_time", joinColumns = @JoinColumn(name = "survey_id"))
    @Column(name = "spare_time")
    @Cascade(CascadeType.ALL)
    private List<String> spareTime;

    private String preference1;
    private String preference2;
    private String preference3;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "survey_like_option", joinColumns = @JoinColumn(name = "survey_id"))
    @Column(name = "like_option")
    @Cascade(CascadeType.ALL)
    private List<String> likeOption;

    private String extraRequest;

}
