package com.apple.sobok.oauth;

import com.apple.sobok.jwt.JwtUtil;
import com.apple.sobok.member.Member;
import com.apple.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final PeopleApiService peopleApiService;
    private final MemberService memberService;
    private final OauthAccountRepository oauthAccountRepository;
    private final JwtUtil jwtUtil;


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



        // 4. JWT 생성
//        Authentication authToken = new UsernamePasswordAuthenticationToken(oidcUser, null, oidcUser.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//        var auth1 = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("auth : " + auth1);
//        if (auth1 != null) {
//            String jwt = jwtUtil.createToken(auth1);
//            String refreshToken = jwtUtil.createRefreshToken(auth1);
//        }



        return oidcUser;
    }
}