package com.apple.sobok.survey;


import com.apple.sobok.routine.AiRoutine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String OPENAI_API_KEY = "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";

    // AI 추천 루틴 생성
    public List<Map<String, String>> generateAiRoutine(Survey survey) {
        // AI에 전달할 입력 데이터 생성
        String prompt = createPromptFromSurvey(survey);

        // AI API 호출
        String aiResponse = callOpenAiApi(prompt);
        System.out.println("AI response: " + aiResponse); // 디버깅용 출력

        // AI 응답 데이터 파싱
        return parseAiResponse(aiResponse, survey.getUserId());
    }

    // 설문 데이터를 바탕으로 AI 입력 생성
    private String createPromptFromSurvey(Survey survey) {
        return String.format(
                """
                        다음 정보를 바탕으로 개인 맞춤형 루틴을 생성해 줘:
                        직업: %s
                        자투리 시간: %s
                        루틴 속성: %s, %s, %s
                        취향: %s
                        다음과 같은 JSON 형식으로 결과를 제공해줘 (예시):
                        [
                          {
                            "title": "아침 운동",
                            "start_time": "06:00",
                            "end_time": "07:00"
                          },
                          {
                            "title": "저녁 독서",
                            "start_time": "20:00",
                            "end_time": "21:00"
                          }
                        ]
                        응답 값을 바로 사용할 수 있도록 JSON 형식으로 제공해줘.
                        """,
                survey.getOccupation(),
                survey.getSpareTime(),
                survey.getPreference1(),
                survey.getPreference2(),
                survey.getPreference3(),
                survey.getLikeOption()
        );
    }

    // OpenAI API 호출
    private String callOpenAiApi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("Prompt being sent to API: " + prompt); // 디버깅용 출력
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an AI assistant."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        // 요청 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // JSON 형식 설정
        headers.setBearerAuth(OPENAI_API_KEY); // OpenAI API 키 설정

        // 요청 엔터티 생성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // OpenAI API 호출
        return restTemplate.postForObject(OPENAI_API_URL, requestEntity, String.class);
    }

    // AI 응답 데이터 파싱
    private List<Map<String, String>> parseAiResponse(String aiResponse, Long userId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);

            // 1. content 필드 가져오기
            String rawContent = rootNode.path("choices").get(0).path("message").path("content").asText();

            // 2. 불필요한 부분 제거
            String cleanedContent = rawContent
                    .replaceAll("^```json", "") // 시작의 ```json 제거
                    .replaceAll("```$", "")   // 끝의 ``` 제거
                    .trim();                 // 앞뒤 공백 제거

            // 3. JSON 파싱
            List<Map<String, String>> routines = objectMapper.readValue(cleanedContent, new TypeReference<>() {});
            return routines;

            // List<AiRoutine> aiRoutines = new ArrayList<>();
//            for (Map<String, String> routine : routines) {
//                AiRoutine aiRoutine = new AiRoutine();
//                aiRoutine.setTitle(routine.get("title"));
//                aiRoutine.setStartTime(routine.get("start_time"));
//                aiRoutine.setEndTime(routine.get("end_time"));
//                aiRoutine.setUserId(userId);
//                aiRoutines.add(aiRoutine);
//            }
//
//            return aiRoutines;
//
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI 응답 파싱 중 오류 발생: " + e.getMessage());
        }
    }


}
