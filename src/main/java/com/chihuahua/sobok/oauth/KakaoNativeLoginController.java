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
public class KakaoNativeLoginController {

  private final KakaoTokenVerifier kakaoTokenVerifier;
  private final JwtUtil jwtUtil;
  private final OauthAccountRepository oauthAccountRepository;
  private final MemberRepository memberRepository;

  @PostMapping("/kakao/native")
  public ResponseEntity<?> kakaoNativeLogin(@RequestBody KakaoNativeLoginRequest request) {
    try {
      log.info("카카오 네이티브 로그인 요청 수신");

      // 카카오 액세스 토큰 검증 및 사용자 정보 추출
      KakaoUserInfo userInfo = kakaoTokenVerifier.verifyAccessToken(request.getAccessToken());

      // 이메일이 null인 경우 처리
      if (userInfo.getEmail() == null || userInfo.getEmail().trim().isEmpty()) {
        log.warn("카카오 로그인에서 이메일 정보가 없습니다. id: {}", userInfo.getId());
        return ResponseEntity.badRequest().body(Map.of("error", "이메일 정보가 필요합니다."));
      }

      // 기존 OAuth 계정이 있는지 확인
      Optional<OauthAccount> existingOauthAccount =
          oauthAccountRepository.findByOauthIdAndProvider(userInfo.getId(), "kakao");

      Member member;
      boolean isExistingUser = existingOauthAccount.isPresent();

      if (isExistingUser) { // 1. 기존 카카오 OAuth 계정이 있는 경우
        member = existingOauthAccount.get().getMember();

        log.info("기존 카카오 OAuth 계정으로 로그인: {}", member.getEmail());
      } else {
        // 일반 이메일인 경우 (카카오는 이메일 가리기 기능이 없음)
        Optional<Member> existingMember = memberRepository.findByEmail(userInfo.getEmail());

        if (existingMember.isEmpty()) {
          // 기존 회원이 없는 경우 - 새 회원 생성
          Member newMember = new Member();
          newMember.setEmail(userInfo.getEmail());
          newMember.setUsername(userInfo.getId()); // 카카오 ID를 username으로 사용
          newMember.setDisplayName(userInfo.getNickname()); // 카카오 닉네임을 displayName으로 사용
          newMember.setIsOauth(true);
          member = memberRepository.save(newMember);

          // OAuth 계정 정보 저장
          OauthAccount newAccount = new OauthAccount();
          newAccount.setOauthId(userInfo.getId());
          newAccount.setProvider("kakao");
          newAccount.setMember(member);
          oauthAccountRepository.save(newAccount);

          log.info("새로운 카카오 회원 생성: {}", member.getEmail());
        } else {
          // 기존 회원이 있는 경우
          member = existingMember.get();

          // 이미 카카오 OAuth 계정이 연결되어 있는지 재확인 (다른 id로 등록된 경우)
          Optional<OauthAccount> existingKakaoAccount =
              oauthAccountRepository.findByMemberAndProvider(member, "kakao");

          if (existingKakaoAccount.isPresent()) {
            log.warn(
                "기존 회원에게 이미 다른 카카오 OAuth 계정이 연결되어 있습니다. email: {}, existing_id: {}, new_id: {}",
                member.getEmail(), existingKakaoAccount.get().getOauthId(), userInfo.getId());
            return ResponseEntity.badRequest().body(Map.of("error", "이미 다른 카카오 계정이 연결되어 있습니다."));
          }

          log.info("기존 회원에 카카오 OAuth 계정 연결: {}", member.getEmail());

          // OAuth 계정 정보 저장
          OauthAccount newAccount = new OauthAccount();
          newAccount.setOauthId(userInfo.getId());
          newAccount.setProvider("kakao");
          newAccount.setMember(member);
          oauthAccountRepository.save(newAccount);
        }
      }

      // JWT 토큰 생성
      String accessToken = jwtUtil.createTokenForKakaoUser(userInfo);
      String refreshToken = jwtUtil.createRefreshTokenForKakaoUser(userInfo);

      return ResponseEntity.ok(Map.of(
          "accessToken", accessToken,
          "refreshToken", refreshToken,
          "user", Map.of(
              "id", member.getId(),
              "email", member.getEmail(),
              "name", member.getName(),
              "displayName", member.getDisplayName(),
              "provider", "kakao",
              "isExistingUser", isExistingUser // 기존 사용자 여부
          )
      ));

    } catch (Exception e) {
      log.error("카카오 네이티브 로그인 실패", e);
      return ResponseEntity.badRequest().body(Map.of("error", "로그인 실패: " + e.getMessage()));
    }
  }
}
