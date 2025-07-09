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

      // 이메일이 null인 경우 처리
      if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
        log.warn("Apple 로그인에서 이메일 정보가 없습니다. sub: {}", userInfo.getSub());
        return ResponseEntity.badRequest().body(Map.of("error", "이메일 정보가 필요합니다."));
      }

      // 기존 OAuth 계정이 있는지 확인
      Optional<OauthAccount> existingOauthAccount =
          oauthAccountRepository.findByOauthIdAndProvider(userInfo.getSub(), "apple");

      Member member;

      boolean isExistingUser = existingOauthAccount.isPresent();

      if (isExistingUser) { // 1. 기존 Apple OAuth 계정이 있는 경우
        member = existingOauthAccount.get().getMember();

        log.info("기존 Apple OAuth 계정으로 로그인: {}", member.getEmail());
      } else {
        if (userInfo.getEmail().endsWith("@privaterelay.appleid.com")) {
          // Apple Private Relay 이메일인 경우 - 항상 새 계정 생성
          Member newMember = new Member();
          newMember.setEmail(userInfo.getEmail());
          newMember.setUsername(userInfo.getSub());
          newMember.setIsOauth(true);
          member = memberRepository.save(newMember);

          // OAuth 계정 정보 저장
          OauthAccount newAccount = new OauthAccount();
          newAccount.setOauthId(userInfo.getSub());
          newAccount.setProvider("apple");
          newAccount.setMember(member);
          oauthAccountRepository.save(newAccount);

          log.info("새로운 Apple 회원 생성 (Private Relay): {}", member.getEmail());
        } else {
          // 일반 이메일인 경우
          Optional<Member> existingMember = memberRepository.findByEmail(userInfo.getEmail());

          if (existingMember.isEmpty()) {
            // 기존 회원이 없는 경우 - 새 회원 생성
            Member newMember = new Member();
            newMember.setEmail(userInfo.getEmail());
            newMember.setUsername(userInfo.getSub());
            newMember.setIsOauth(true);
            member = memberRepository.save(newMember);

            // OAuth 계정 정보 저장
            OauthAccount newAccount = new OauthAccount();
            newAccount.setOauthId(userInfo.getSub());
            newAccount.setProvider("apple");
            newAccount.setMember(member);
            oauthAccountRepository.save(newAccount);

            log.info("새로운 Apple 회원 생성 (일반 이메일): {}", member.getEmail());
          } else {
            // 기존 회원이 있는 경우
            member = existingMember.get();

            // 이미 Apple OAuth 계정이 연결되어 있는지 재확인 (다른 sub로 등록된 경우)
            Optional<OauthAccount> existingAppleAccount =
                oauthAccountRepository.findByMemberAndProvider(member, "apple");

            if (existingAppleAccount.isPresent()) {
              log.warn(
                  "기존 회원에게 이미 다른 Apple OAuth 계정이 연결되어 있습니다. email: {}, existing_sub: {}, new_sub: {}",
                  member.getEmail(), existingAppleAccount.get().getOauthId(), userInfo.getSub());
              return ResponseEntity.badRequest()
                  .body(Map.of("error", "이미 다른 Apple 계정이 연결되어 있습니다."));
            }

            log.info("기존 회원에 Apple OAuth 계정 연결: {}", member.getEmail());

            // OAuth 계정 정보 저장
            OauthAccount newAccount = new OauthAccount();
            newAccount.setOauthId(userInfo.getSub());
            newAccount.setProvider("apple");
            newAccount.setMember(member);
            oauthAccountRepository.save(newAccount);
          }
        }
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
              "isExistingUser", isExistingUser // 기존 사용자 여부
          )
      ));

    } catch (Exception e) {
      log.error("Apple 네이티브 로그인 실패", e);
      return ResponseEntity.badRequest().body(Map.of("error", "로그인 실패: " + e.getMessage()));
    }
  }
}
