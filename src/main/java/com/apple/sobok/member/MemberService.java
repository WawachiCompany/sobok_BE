package com.apple.sobok.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
