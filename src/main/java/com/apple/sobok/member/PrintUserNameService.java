package com.apple.sobok.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Service
@RequiredArgsConstructor
public class PrintUserNameService {

    private final MemberRepository memberRepository;

    public String printUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("authentication = " + authentication);
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OidcUser oidcUser) {
                Optional<Member> member = memberRepository.findByEmail(oidcUser.getEmail());
                return member.get().getUsername();

            } else if (principal instanceof OAuth2User oauth2User) {
                Optional<Member> member = memberRepository.findByEmail(oauth2User.getAttribute("email"));
                return member.get().getUsername();

            } else {
                return authentication.getName();
            }
        }
        return null;
    }
}
