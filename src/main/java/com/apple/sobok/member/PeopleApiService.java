package com.apple.sobok.member;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PeopleApiService {

    public String getBirthdays(String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me?personFields=birthdays";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {});

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("birthdays")) {
            // birthdays 필드 추출
            List<Map<String, Object>> birthdays = (List<Map<String, Object>>) body.get("birthdays");
            if (!birthdays.isEmpty()) {
                Map<String, Object> date = (Map<String, Object>) birthdays.get(0).get("date");
                int year = (int) date.get("year");
                int month = (int) date.get("month");
                int day = (int) date.get("day");

                // 생년월일 조합하여 반환
                return String.format("%04d%02d%02d", year, month, day);
            }
        }
        return "No birthdays found";
    }
}
