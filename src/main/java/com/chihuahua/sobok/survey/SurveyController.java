package com.chihuahua.sobok.survey;


import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
        } catch (ResponseStatusException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "액세스 토큰 만료: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        catch (Exception e) {
            return ResponseEntity.badRequest().body("설문 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/result")
    public ResponseEntity<?> getSurveyResult() {
        try {
            Member member = memberService.getMember();
            return surveyService.getSurveyResult(member);
        } catch (ResponseStatusException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "액세스 토큰 만료: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        catch (Exception e) {
            return ResponseEntity.badRequest().body("설문 결과 조회 중 오류 발생: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteSurvey() {
        try {
            Member member = memberService.getMember();
            return surveyService.deleteSurvey(member);
        } catch (ResponseStatusException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "액세스 토큰 만료: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("설문 결과 삭제 중 오류 발생: " + e.getMessage());
        }
    }

}
