package com.chihuahua.sobok.oauth;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import com.nimbusds.jose.JOSEException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final PeopleApiService peopleApiService;
    @Lazy
    private final MemberService memberService;
    private final OauthAccountRepository oauthAccountRepository;
    private final KakaoIdTokenValidator kakaoIdTokenValidator;
    private final ApplePublicKeyService applePublicKeyService;


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
            OauthAccount oauthAccount = oauthAccountRepository.findByOauthIdAndProvider(kakaoId, registrationId)
                    .orElseGet(() -> {
                        OauthAccount newAccount = new OauthAccount();
                        newAccount.setOauthId(kakaoId);
                        newAccount.setProvider(registrationId);
                        newAccount.setCreatedAt(LocalDateTime.now());
                        newAccount.setMember(member);
                        return oauthAccountRepository.save(newAccount);
                    });

        }
        else if ("google".equals(registrationId)) {


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
                        newMember.setCreatedAt(LocalDateTime.now());
                        newMember.setPoint(0);
                        newMember.setUsername(oauthId);
                        newMember.setIsOauth(true);
                        return memberService.saveOrUpdate(newMember);
                    });

            // 3. oauth_account 테이블에 저장
            OauthAccount oauthAccount = oauthAccountRepository.findByOauthIdAndProvider(oauthId, userRequest.getClientRegistration().getRegistrationId())
                    .orElseGet(() -> {
                        OauthAccount newAccount = new OauthAccount();
                        newAccount.setOauthId(oauthId);
                        newAccount.setProvider(userRequest.getClientRegistration().getRegistrationId());
                        newAccount.setCreatedAt(LocalDateTime.now());
                        newAccount.setMember(member);
                        System.out.println("Saving new OauthAccount: " + newAccount);
                        return oauthAccountRepository.save(newAccount);
                    });

        } else if("apple".equals(registrationId)) {
            try {
                // Apple 로그인 시 id_token 검증
                String idToken = userRequest.getIdToken().getTokenValue();
                Claims claims = (Claims) applePublicKeyService.verifyToken(idToken);

                //1. Apple 로그인 정보 가져오기
                String appleId = claims.get("sub", String.class);
                String appleEmail = claims.get("email", String.class);
                String appleName = claims.get("name", String.class);

                // 2. member 테이블에 저장 (회원가입 이력이 없는 경우)
                Member member = memberService.findByEmail(appleEmail)
                        .map(existingMember -> {
                            existingMember.setIsOauth(true);
                            return memberService.saveOrUpdate(existingMember); // 이메일 정보가 있는 경우에는 oauth 정보만 업데이트
                        })
                        .orElseGet(() -> {
                            Member newMember = new Member();
                            newMember.setEmail(appleEmail);
                            newMember.setName(appleName);
                            newMember.setCreatedAt(LocalDateTime.now());
                            newMember.setPoint(0);
                            newMember.setUsername(appleId);
                            newMember.setIsOauth(true);
                            return memberService.saveOrUpdate(newMember);
                        });

                // 3. oauth_account 테이블에 저장
                OauthAccount oauthAccount = oauthAccountRepository.findByOauthIdAndProvider(appleId, registrationId)
                        .orElseGet(() -> {
                            OauthAccount newAccount = new OauthAccount();
                            newAccount.setOauthId(appleId);
                            newAccount.setProvider(registrationId);
                            newAccount.setCreatedAt(LocalDateTime.now());
                            newAccount.setMember(member);
                            return oauthAccountRepository.save(newAccount);
                        });
            } catch (ParseException | IOException | JOSEException e) {
                throw new RuntimeException(e);
            }
        }

        OidcUser oidcUser = super.loadUser(userRequest);

// 사용자 고유 식별자
        String userId = oidcUser.getName();
        System.out.println("userId: " + userId);

// 사용자 속성 (예: 이메일, 이름 등)
        Map<String, Object> attributes = oidcUser.getAttributes();
        System.out.println("attributes: " + attributes);

// 사용자 권한
        Collection<? extends GrantedAuthority> authorities = oidcUser.getAuthorities();
        System.out.println("authorities: " + authorities);

// ID 토큰
        OidcIdToken idToken = oidcUser.getIdToken();
        System.out.println("idToken: " + idToken);

// 사용자 정보
        OidcUserInfo userInfo = oidcUser.getUserInfo();
        System.out.println("userInfo: " + userInfo);

        return oidcUser;
    }
}