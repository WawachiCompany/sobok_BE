package com.chihuahua.sobok.oauth;

import com.chihuahua.sobok.jwt.JwtUtil;
import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberRepository;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AppleNativeLoginController {

  private final AppleTokenVerifier appleTokenVerifier;
  private final JwtUtil jwtUtil;
  private final OauthAccountRepository oauthAccountRepository;
  private final MemberRepository memberRepository;

  @PostMapping("/apple/native")
  public ResponseEntity<?> appleNativeLogin(@RequestBody AppleNativeLoginRequest request) {
    try {
      log.info("Apple 네이티브 로그인 요청 수신");

      // Apple JWT 토큰 검증
      AppleUserInfo userInfo = appleTokenVerifier.verifyIdentityToken(request.getIdentityToken());

      // 1. 기존 OAuth 계정이 있는지 확인
      Optional<OauthAccount> existingOauthAccount =
          oauthAccountRepository.findByOauthIdAndProvider(userInfo.getSub(), "apple");

      Member member;

      if (existingOauthAccount.isPresent()) {
        // 기존 OAuth 계정이 있으면 해당 회원 사용
        member = existingOauthAccount.get().getMember();
        log.info("기존 Apple OAuth 계정으로 로그인: {}", member.getEmail());
      } else {
        // 2. Apple private relay 이메일이므로 기존 회원과 자동 연결하지 않음
        // 새로운 회원으로 생성
        Member newMember = new Member();
        newMember.setEmail(userInfo.getEmail()); // private relay 이메일
        newMember.setName(userInfo.getName() != null ? userInfo.getName() : "Apple User");
        newMember.setUsername(userInfo.getSub());
        newMember.setIsOauth(true);
        member = memberRepository.save(newMember);

        // OAuth 계정 정보 저장
        OauthAccount newAccount = new OauthAccount();
        newAccount.setOauthId(userInfo.getSub());
        newAccount.setProvider("apple");
        newAccount.setMember(member);
        oauthAccountRepository.save(newAccount);

        log.info("새로운 Apple 회원 생성: {}", member.getEmail());
      }

      // JWT 토큰 생성
      String accessToken = jwtUtil.createTokenForAppleUser(userInfo);
      String refreshToken = jwtUtil.createRefreshTokenForAppleUser(userInfo);

      return ResponseEntity.ok(Map.of(
          "accessToken", accessToken,
          "refreshToken", refreshToken,
          "user", Map.of(
              "id", member.getId(),
              "email", member.getEmail(),
              "name", member.getName(),
              "provider", "apple",
              "isNewUser", existingOauthAccount.isEmpty() // 신규 사용자 여부
          )
      ));

    } catch (Exception e) {
      log.error("Apple 네이티브 로그인 실패", e);
      return ResponseEntity.badRequest().body(Map.of("error", "로그인 실패: " + e.getMessage()));
    }
  }
}
