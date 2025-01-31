package com.apple.sobok.survey;


import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberRepository;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final AIRecommendationService aiRecommendationService;
    private final SurveyRepository surveyRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;


    @PostMapping("/generate")
    public ResponseEntity<?> generateRoutine(@RequestBody SurveyRequestDto surveyRequestDto) {
        try {
            Survey survey = surveyService.saveSurvey(surveyRequestDto);
            Map<String, Object> routine = aiRecommendationService.generateAiRoutine(survey);

            return ResponseEntity.ok(routine);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("설문 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/result")
    public ResponseEntity<?> getSurveyResult() {
        try {
            Member member = memberService.getMember();
            return surveyService.getSurveyResult(member);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("설문 결과 조회 중 오류 발생: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteSurvey() {
        try {
            Member member = memberService.getMember();
            return surveyService.deleteSurvey(member);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("설문 결과 삭제 중 오류 발생: " + e.getMessage());
        }
    }

}
