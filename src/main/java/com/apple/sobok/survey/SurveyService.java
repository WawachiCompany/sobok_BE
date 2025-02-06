package com.apple.sobok.survey;

import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class SurveyService {
    
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;


    // 설문 데이터 저장
    @Transactional
    public synchronized Survey saveSurvey(SurveyRequestDto surveyRequestDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 기존 설문 데이터 존재하면 삭제
        Optional<Survey> existingSurvey = surveyRepository.findByMember(member);
        Survey survey;
        if (existingSurvey.isPresent()) {
            survey = existingSurvey.get();
            survey.setSpareTpo(surveyRequestDto.getSpareTpo());
            survey.setSpareTime(surveyRequestDto.getSpareTime());
            survey.setPreference1(surveyRequestDto.getPreference1());
            survey.setPreference2(surveyRequestDto.getPreference2());
            survey.setPreference3(surveyRequestDto.getPreference3());
            survey.setLikeOption(surveyRequestDto.getLikeOption());
            if(surveyRequestDto.getExtraRequest() != null) {
                survey.setExtraRequest(surveyRequestDto.getExtraRequest());
            }
            else {
                survey.setExtraRequest("없음");
            }
        } else {
            survey = new Survey();
            survey.setSpareTpo(surveyRequestDto.getSpareTpo());
            survey.setSpareTime(surveyRequestDto.getSpareTime());
            survey.setPreference1(surveyRequestDto.getPreference1());
            survey.setPreference2(surveyRequestDto.getPreference2());
            survey.setPreference3(surveyRequestDto.getPreference3());
            survey.setLikeOption(surveyRequestDto.getLikeOption());
            survey.setMember(member);
            if(surveyRequestDto.getExtraRequest() != null) {
                survey.setExtraRequest(surveyRequestDto.getExtraRequest());
            }
            else{
                survey.setExtraRequest("없음");
            }
        }

        return surveyRepository.save(survey);
    }

    public ResponseEntity<?> getSurveyResult(Member member) {
        Optional<Survey> result = surveyRepository.findByMember(member);
        if (result.isEmpty()) {
            return ResponseEntity.badRequest().body("설문 결과가 존재하지 않습니다.");
        }
        Survey survey = result.get();
        SurveyRequestDto surveyRequestDto = new SurveyRequestDto();
        surveyRequestDto.setSpareTime(survey.getSpareTime());
        surveyRequestDto.setPreference1(survey.getPreference1());
        surveyRequestDto.setPreference2(survey.getPreference2());
        surveyRequestDto.setPreference3(survey.getPreference3());
        surveyRequestDto.setLikeOption(survey.getLikeOption());
        return ResponseEntity.ok(surveyRequestDto);
    }

    public ResponseEntity<?> deleteSurvey(Member member) {
        Optional<Survey> survey = surveyRepository.findByMember(member);
        if (survey.isEmpty()) {
            return ResponseEntity.badRequest().body("설문 결과가 존재하지 않습니다.");
        }
        surveyRepository.delete(survey.get());
        return ResponseEntity.ok("설문 결과 삭제 성공");
    }



}
