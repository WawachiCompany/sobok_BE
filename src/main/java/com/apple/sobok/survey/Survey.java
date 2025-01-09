package com.apple.sobok.survey;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String occupation;

    @ElementCollection
    @CollectionTable(name = "survey_spare_time", joinColumns = @JoinColumn(name = "survey_id"))
    @Column(name = "spare_time")
    private List<String> spareTime;

    private String preference1;
    private String preference2;
    private String preference3;

    private String likeOption;

}
