package com.chihuahua.sobok.oauth;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

  private final PeopleApiService peopleApiService;
  @Lazy
  private final MemberService memberService;
  private final OauthAccountRepository oauthAccountRepository;
  private final KakaoIdTokenValidator kakaoIdTokenValidator;

  @Override
  @Transactional
  public OidcUser loadUser(OidcUserRequest userRequest) {

    //클라이언트 등록 ID 확인
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    //0. 카카오로 로그인한 경우 id 토큰 검증 및 정보 가져오기
    if ("kakao".equals(registrationId)) {

      // 카카오 로그인 시 id_token 검증
      String idToken = userRequest.getIdToken().getTokenValue();
      Claims claims = kakaoIdTokenValidator.validateIdToken(idToken);

      //1. 카카오 로그인 정보 가져오기
      String kakaoId = claims.get("sub", String.class);
      String kakaoEmail = claims.get("email", String.class);
      String kakaoNickname = claims.get("nickname", String.class);

      // 2. member 테이블에 저장 (회원가입 이력이 없는 경우)
      Member member = memberService.findByEmail(kakaoEmail)
          .map(existingMember -> {
            existingMember.setIsOauth(true);
            return memberService.saveOrUpdate(existingMember); // 이메일 정보가 있는 경우에는 oauth 정보만 업데이트
          })
          .orElseGet(() -> {
            Member newMember = new Member();
            newMember.setEmail(kakaoEmail);
            newMember.setName(kakaoNickname);
            newMember.setCreatedAt(LocalDateTime.now());
            newMember.setPoint(0);
            newMember.setUsername(kakaoId);
            newMember.setIsOauth(true);
            return memberService.saveOrUpdate(newMember);
          });

      // 3. oauth_account 테이블에 저장
      oauthAccountRepository.findByOauthIdAndProvider(kakaoId, registrationId)
          .orElseGet(() -> {
            OauthAccount newAccount = new OauthAccount();
            newAccount.setOauthId(kakaoId);
            newAccount.setProvider(registrationId);
            newAccount.setMember(member);
            return oauthAccountRepository.save(newAccount);
          });

    } else if ("google".equals(registrationId)) {

      // OICD User 정보 가져오기
      OidcUser oidcUser = super.loadUser(userRequest);

      // 1. 사용자 정보 가져오기
      Map<String, Object> attributes = oidcUser.getAttributes();
      System.out.println(attributes);
      String oauthId = oidcUser.getName(); // OAuth에서 제공하는 고유 ID
      String email = (String) attributes.get(StandardClaimNames.EMAIL);
      String name = (String) attributes.get(StandardClaimNames.NAME);

      // People API 호출
      String accessToken = userRequest.getAccessToken().getTokenValue();
      String birthdate = peopleApiService.getBirthdays(accessToken);

      // 2. member 테이블에 저장 (회원가입 이력이 없는 경우)
      Member member = memberService.findByEmail(email)
          .map(existingMember -> {
            existingMember.setIsOauth(true);
            return memberService.saveOrUpdate(existingMember); // 이메일 정보가 있는 경우에는 oauth 정보만 업데이트
          })
          .orElseGet(() -> {
            Member newMember = new Member();
            newMember.setEmail(email);
            newMember.setName(name);
            newMember.setBirth(birthdate);
            newMember.setUsername(oauthId);
            newMember.setIsOauth(true);
            return memberService.saveOrUpdate(newMember);
          });

      // 3. oauth_account 테이블에 저장
      oauthAccountRepository.findByOauthIdAndProvider(oauthId,
              userRequest.getClientRegistration().getRegistrationId())
          .orElseGet(() -> {
            OauthAccount newAccount = new OauthAccount();
            newAccount.setOauthId(oauthId);
            newAccount.setProvider(userRequest.getClientRegistration().getRegistrationId());
            newAccount.setMember(member);
            return oauthAccountRepository.save(newAccount);
          });
    }
    // Apple 관련 코드 제거 - 이제 네이티브 API로 처리됨

    return super.loadUser(userRequest);
  }
}