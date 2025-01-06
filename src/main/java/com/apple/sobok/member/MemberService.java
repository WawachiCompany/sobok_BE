package com.apple.sobok.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean isPhoneNumberDuplicated(String phoneNumber) {
        return memberRepository.existsByPhoneNumber(phoneNumber);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // MemberController에서 유저 정보 조회
    public Map<String, Object> getUserInfo(Authentication auth) {
        var user = (MyUserDetailsService.CustomUser) auth.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("id", user.id);
        response.put("name", user.name);
        response.put("displayName", user.displayName);
        response.put("point", user.point);
        response.put("email", user.email);
        response.put("phoneNumber", user.phoneNumber);
        response.put("birth", user.birth);
        response.put("message", "유저 정보 조회 성공");
        return response;
    }

    @Transactional
    public Member saveOrUpdate(Member member) {
        var result = memberRepository.findByUsername(member.getUsername());
        if (result.isPresent()) {
            Member existingMember = result.get();
            existingMember.setName(member.getName());
            existingMember.setEmail(member.getEmail());
            existingMember.setBirth(member.getBirth());
            return memberRepository.save(existingMember);
        } else {
            return memberRepository.save(member);
        }
    }
}


