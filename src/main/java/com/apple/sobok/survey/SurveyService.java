package com.apple.sobok.survey;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class SurveyService {
    
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    
    private static final Map<String, String> LIKE_OPTIONS = Map.of(
            "reading", "독서",
            "movie", "영화",
            "music", "음악",
            "exercise", "운동",
            "game", "게임",
            "cooking", "요리"
    );

    private static final Map<String, String> OCCUPATIONS = Map.of(
            "student", "학생",
            "housekeeper", "주부",
            "officer", "회사원",
            "self-employed", "자영업자"
    );


    public Map<String, String> getAllOptions() {
        return LIKE_OPTIONS;
    }

    public Map<String, String> getOccupations() {
        return OCCUPATIONS;
    }

    // 설문 데이터 저장
    @Transactional
    public Survey saveSurvey(SurveyRequestDto surveyRequestDto, Long userId) {
        Member user = memberRepository.findById(userId) // 사용자 정보 조회
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Survey survey = new Survey();
        survey.setOccupation(surveyRequestDto.getOccupation());
        survey.setSpareTime(surveyRequestDto.getSpareTime());
        survey.setPreference1(surveyRequestDto.getPreference1());
        survey.setPreference2(surveyRequestDto.getPreference2());
        survey.setPreference3(surveyRequestDto.getPreference3());
        survey.setLikeOption(surveyRequestDto.getLikeOption());
        survey.setUserId(user.getId());

        return surveyRepository.save(survey);
    }



}
