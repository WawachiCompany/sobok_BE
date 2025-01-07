package com.apple.sobok;

import com.apple.sobok.member.*;
import com.apple.sobok.oauth.CustomOAuth2UserService;
import com.apple.sobok.oauth.OauthAccount;
import com.apple.sobok.oauth.OauthAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails.UserInfoEndpoint;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock
    private MemberService memberService;

    @Mock
    private OauthAccountRepository oauthAccountRepository;

    @Mock
    private OidcUser oidcUser;

    @Mock
    private OidcUserRequest oidcUserRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private ProviderDetails providerDetails;

    @Mock
    private UserInfoEndpoint userInfoEndpoint;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUser_NewOauthAccount_NewMember() {
        // Mock ClientRegistration and ProviderDetails
        when(oidcUserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(userInfoEndpoint);
        when(userInfoEndpoint.getUri()).thenReturn("https://openidconnect.googleapis.com/v1/userinfo");

        // Mock OIDC attributes
        when(oidcUser.getAttributes()).thenReturn(Map.of(
                "email", "test@example.com",
                "name", "John Doe"
        ));
        when(oidcUser.getName()).thenReturn("oauth-id-123");

        // Mock repository responses
        when(oauthAccountRepository.findByOauthIdAndProvider("oauth-id-123", "google"))
                .thenReturn(Optional.empty());
        when(oauthAccountRepository.save(any(OauthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(memberService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(memberService.saveOrUpdate(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the method under test
        customOAuth2UserService.loadUser(oidcUserRequest);

        // Verify repository interactions
        verify(oauthAccountRepository, times(1)).save(any(OauthAccount.class));
        verify(memberService, times(1)).saveOrUpdate(any(Member.class));
    }
}