package com.chihuahua.sobok.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleTokenVerifier {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

  public GoogleUserInfo verifyIdToken(String idToken) {
    try {
      // 구글 tokeninfo API로 ID Token 검증
      String url = GOOGLE_TOKEN_INFO_URL + idToken;

      ResponseEntity<String> response = restTemplate.exchange(
          url,
          org.springframework.http.HttpMethod.GET,
          null,
          String.class
      );

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new RuntimeException("구글 ID Token 검증 실패: " + response.getStatusCode());
      }

      // JSON 응답 파싱
      JsonNode jsonNode = objectMapper.readTree(response.getBody());

      // 토큰이 유효하지 않은 경우
      if (jsonNode.has("error")) {
        throw new RuntimeException(
            "구글 ID Token이 유효하지 않습니다: " + jsonNode.get("error_description").asText());
      }

      GoogleUserInfo userInfo = new GoogleUserInfo();
      userInfo.setSub(jsonNode.get("sub").asText());
      userInfo.setEmail(jsonNode.get("email").asText());

      if (jsonNode.has("name")) {
        userInfo.setName(jsonNode.get("name").asText());
      }

      if (jsonNode.has("picture")) {
        userInfo.setPicture(jsonNode.get("picture").asText());
      }

      log.info("구글 사용자 정보 조회 성공: sub={}, email={}", userInfo.getSub(), userInfo.getEmail());
      return userInfo;

    } catch (Exception e) {
      log.error("구글 ID Token 검증 실패", e);
      throw new RuntimeException("구글 ID Token 검증 실패: " + e.getMessage());
    }
  }
}
