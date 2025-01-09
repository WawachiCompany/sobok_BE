package com.apple.sobok.survey;


import com.apple.sobok.routine.AiRoutine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final AIRecommendationService aiRecommendationService;

    //취향 옵션 Map 전송
    @GetMapping("/survey/like-options")
    public ResponseEntity<Map<String, String>> getLikeOptions() {
        Map<String, String> likeOptions = surveyService.getAllOptions();
        return ResponseEntity.ok(likeOptions);
    }

    //취향 옵션 Map 전송
    @GetMapping("/survey/occupations")
    public ResponseEntity<Map<String, String>> getOccupations() {
        Map<String, String> occupations = surveyService.getOccupations();
        return ResponseEntity.ok(occupations);
    }

    @GetMapping("/survey")
    public String survey() {
        return "survey.html";
    }

    @PostMapping("/survey/generate")
    public ResponseEntity<?> generateRoutine(@RequestBody SurveyRequestDto surveyRequestDto, @RequestParam Long userId) {
        try {
            Survey survey = surveyService.saveSurvey(surveyRequestDto, userId);
            List<Map<String, String>> routine = aiRecommendationService.generateAiRoutine(survey);
            return ResponseEntity.ok(routine);
        } catch (Exception e) {
            System.out.println("설문 저장 중 오류 발생: " + e.getMessage());
            return ResponseEntity.badRequest().body("설문 저장 중 오류 발생: " + e.getMessage());
        }
    }

}
