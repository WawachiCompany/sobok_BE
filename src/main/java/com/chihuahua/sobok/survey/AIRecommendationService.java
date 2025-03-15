package com.chihuahua.sobok.survey;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AIRecommendationService {
    @Value("${openai.api.url}")
    private String OPENAI_API_URL;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    // AI 추천 루틴 생성 (MAIN)
    public Map<String, Object> generateAiRoutine(Survey survey) {
        // AI에 전달할 입력 데이터 생성
        String prompt = createPromptFromSurvey(survey);

        // AI API 호출
        String aiResponse = callOpenAiApi(prompt);
        System.out.println("AI response: " + aiResponse); // 디버깅용 출력

        // AI 응답 데이터 파싱
        return parseAiResponse(aiResponse);
    }

    // 설문 데이터를 바탕으로 AI 입력 생성
    private String createPromptFromSurvey(Survey survey) {
        return String.format(
                """
                        다음 정보를 바탕으로 개인 맞춤형 루틴을 생성해 줘:
                        자투리 시간이 생기는 상황: %s
                        자투리 시간: %s
                        루틴 속성: %s, %s, %s
                        취향: %s
                        요청사항: %s
                        다음과 같은 JSON 형식으로 결과를 제공해줘.
                        이 때 duration의 단위는 분이야.(예시):
                        [
                          {
                            "title": "아침 운동",
                            "start_time": "06:00",
                            "end_time": "07:00",
                            "duration": "60"
                          },
                          {
                            "title": "저녁 독서",
                            "start_time": "20:00",
                            "end_time": "21:00"
                            "duration": "60"
                          }
                        ]
                        여기서 루틴 리스트의 키는 todos야.
                        또한 해당 루틴을 포괄하는 제목도 제공해줘. 키는 title이야.
                        응답 값을 바로 사용할 수 있도록 JSON 형식으로 제공해줘.
                        """,
                survey.getSpareTpo(),
                survey.getSpareTime(),
                survey.getPreference1(),
                survey.getPreference2(),
                survey.getPreference3(),
                survey.getLikeOption(),
                survey.getExtraRequest()
        );
    }

    // OpenAI API 호출
    private String callOpenAiApi(String prompt) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", "너는 사용자의 루틴을 생성하는 AI야."),
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.7
        );

        // 요청 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(OPENAI_API_KEY); // OpenAI API 키 설정

        // 요청 엔터티 생성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // OpenAI API 호출
        return restTemplate.postForObject(OPENAI_API_URL, requestEntity, String.class);
    }

    // AI 응답 데이터 파싱
    private Map<String, Object> parseAiResponse(String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);

            // 1. content 필드 가져오기
            String content = rootNode.path("choices").get(0).path("message").path("content").asText();

            // 2. JSON을 Map으로 변환

            return objectMapper.readValue(content, new TypeReference<>() {
            });

        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 중 오류 발생: " + e.getMessage());
        }
    }


}
