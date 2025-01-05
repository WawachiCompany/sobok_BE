package com.apple.sobok.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/user/create")
    public void createMember(@RequestBody MemberDto memberDto) {
        Member member = new Member();
        member.setName(memberDto.getName());
        member.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 비밀번호 암호화
        member.setDisplayName(memberDto.getDisplayName());

        if(memberService.isEmailDuplicated(memberDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        else {
            member.setEmail(memberDto.getEmail());
        }

        if(memberService.isPhoneNumberDuplicated(memberDto.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
        }
        else {
            member.setPhoneNumber(memberDto.getPhoneNumber());
        }

        member.setBirth(memberDto.getBirth());
        member.setPoint(0);
        member.setCreatedAt(LocalDateTime.now());
        memberRepository.save(member);
    }


}
