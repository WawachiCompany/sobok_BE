package com.apple.sobok.member;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    private final PeopleApiService peopleApiService;
    private final MemberService memberService;
    private final OauthAccountRepository oauthAccountRepository;

    public CustomOAuth2UserService(MemberService memberService, OauthAccountRepository oauthAccountRepository,
                                   PeopleApiService peopleApiService) {
        this.memberService = memberService;
        this.oauthAccountRepository = oauthAccountRepository;
        this.peopleApiService = peopleApiService;
    }


    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) {
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


        // 2. oauth_account 테이블에 저장
        OauthAccount oauthAccount = oauthAccountRepository.findByOauthIdAndProvider(oauthId, userRequest.getClientRegistration().getRegistrationId())
                .orElseGet(() -> {
                    OauthAccount newAccount = new OauthAccount();
                    newAccount.setOauthId(oauthId);
                    newAccount.setProvider(userRequest.getClientRegistration().getRegistrationId());
                    newAccount.setCreatedAt(LocalDateTime.now());
                    System.out.println("Saving new OauthAccount: " + newAccount);
                    return oauthAccountRepository.save(newAccount);
                });

        // 3. member 테이블에 저장 (회원가입 이력이 없는 경우)
        Member member = memberService.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = new Member();
                    newMember.setEmail(email);
                    newMember.setName(name);
                    newMember.setBirth(birthdate);
                    newMember.setCreatedAt(LocalDateTime.now());
                    newMember.setPoint(0);
                    return memberService.saveOrUpdate(newMember);
                });

        // 4. 사용자 반환 (DefaultOidcUser 사용)
        return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}