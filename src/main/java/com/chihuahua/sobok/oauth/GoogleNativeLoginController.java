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
public class GoogleNativeLoginController {

  private final GoogleTokenVerifier googleTokenVerifier;
  private final JwtUtil jwtUtil;
  private final OauthAccountRepository oauthAccountRepository;
  private final MemberRepository memberRepository;

  @PostMapping("/google/native")
  public ResponseEntity<?> googleNativeLogin(@RequestBody GoogleNativeLoginRequest request) {
    try {
      log.info("구글 네이티브 로그인 요청 수신");

      // 구글 ID 토큰 검증 및 사용자 정보 추출
      GoogleUserInfo userInfo = googleTokenVerifier.verifyIdToken(request.getIdToken());

      // 이메일이 null인 경우 처리
      if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
        log.warn("구글 로그인에서 이메일 정보가 없습니다. sub: {}", userInfo.getSub());
        return ResponseEntity.badRequest().body(Map.of("error", "이메일 정보가 필요합니다."));
      }

      // 기존 OAuth 계정이 있는지 확인
      Optional<OauthAccount> existingOauthAccount =
          oauthAccountRepository.findByOauthIdAndProvider(userInfo.getSub(), "google");

      Member member;
      boolean isExistingUser = existingOauthAccount.isPresent();

      if (isExistingUser) { // 1. 기존 구글 OAuth 계정이 있는 경우
        member = existingOauthAccount.get().getMember();

        log.info("기존 구글 OAuth 계정으로 로그인: {}", member.getEmail());
      } else {
        // 일반 이메일인 경우 (구글은 이메일 가리기 기능이 없음)
        Optional<Member> existingMember = memberRepository.findByEmail(userInfo.getEmail());

        if (existingMember.isEmpty()) {
          // 기존 회원이 없는 경우 - 새 회원 생성
          Member newMember = new Member();
          newMember.setEmail(userInfo.getEmail());
          newMember.setUsername(userInfo.getSub()); // 구글 sub를 username으로 사용
          newMember.setIsOauth(true);
          member = memberRepository.save(newMember);

          // OAuth 계정 정보 저장
          OauthAccount newAccount = new OauthAccount();
          newAccount.setOauthId(userInfo.getSub());
          newAccount.setProvider("google");
          newAccount.setMember(member);
          oauthAccountRepository.save(newAccount);

          log.info("새로운 구글 회원 생성: {}", member.getEmail());
        } else {
          // 기존 회원이 있는 경우
          member = existingMember.get();

          // 이미 구글 OAuth 계정이 연결되어 있는지 재확인 (다른 sub로 등록된 경우)
          Optional<OauthAccount> existingGoogleAccount =
              oauthAccountRepository.findByMemberAndProvider(member, "google");

          if (existingGoogleAccount.isPresent()) {
            log.warn(
                "기존 회원에게 이미 다른 구글 OAuth 계정이 연결되어 있습니다. email: {}, existing_sub: {}, new_sub: {}",
                member.getEmail(), existingGoogleAccount.get().getOauthId(), userInfo.getSub());
            return ResponseEntity.badRequest().body(Map.of("error", "이미 다른 구글 계정이 연결되어 있습니다."));
          }

          log.info("기존 회원에 구글 OAuth 계정 연결: {}", member.getEmail());

          // OAuth 계정 정보 저장
          OauthAccount newAccount = new OauthAccount();
          newAccount.setOauthId(userInfo.getSub());
          newAccount.setProvider("google");
          newAccount.setMember(member);
          oauthAccountRepository.save(newAccount);
        }
      }

      // JWT 토큰 생성
      String accessToken = jwtUtil.createTokenForGoogleUser(userInfo);
      String refreshToken = jwtUtil.createRefreshTokenForGoogleUser(userInfo);

      return ResponseEntity.ok(Map.of(
          "accessToken", accessToken,
          "refreshToken", refreshToken,
          "user", Map.of(
              "id", member.getId(),
              "email", member.getEmail() != null ? member.getEmail() : "",
              "provider", "google",
              "isExistingUser", isExistingUser // 기존 사용자 여부
          )
      ));

    } catch (Exception e) {
      log.error("구글 네이티브 로그인 실패", e);
      return ResponseEntity.badRequest().body(Map.of("error", "로그인 실패: " + e.getMessage()));
    }
  }
}
