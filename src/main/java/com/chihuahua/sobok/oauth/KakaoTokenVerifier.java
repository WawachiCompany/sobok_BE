package com.chihuahua.sobok.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoTokenVerifier {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

  public KakaoUserInfo verifyAccessToken(String accessToken) {
    try {
      // 카카오 API로 사용자 정보 요청
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + accessToken);
      headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
          KAKAO_USER_INFO_URL,
          HttpMethod.GET,
          entity,
          String.class
      );

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new RuntimeException("카카오 사용자 정보 조회 실패: " + response.getStatusCode());
      }

      // JSON 응답 파싱
      JsonNode jsonNode = objectMapper.readTree(response.getBody());

      KakaoUserInfo userInfo = new KakaoUserInfo();
      userInfo.setId(jsonNode.get("id").asText());

      // 카카오_계정 정보 추출
      JsonNode kakaoAccount = jsonNode.get("kakao_account");
      if (kakaoAccount != null) {
        if (kakaoAccount.has("email")) {
          userInfo.setEmail(kakaoAccount.get("email").asText());
        }

        // 프로필 정보 추출(닉네임 사용 여부에 따라 주석 처리)
//        JsonNode profile = kakaoAccount.get("profile");
//        if (profile != null) {
//          if (profile.has("nickname")) {
//            userInfo.setNickname(profile.get("nickname").asText());
//          }
//        }
      }

      log.info("카카오 사용자 정보 조회 성공: id={}, email={}", userInfo.getId(), userInfo.getEmail());
      return userInfo;

    } catch (Exception e) {
      log.error("카카오 액세스 토큰 검증 실패", e);
      throw new RuntimeException("카카오 액세스 토큰 검증 실패: " + e.getMessage());
    }
  }
}
