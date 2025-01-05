package com.apple.sobok;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @PostMapping("/user/create")
    public void createMember(Member member) {
        member.setName("test");
        member.setPassword("test");
        member.setDisplayName("test");
        member.setEmail("test");
        member.setPhoneNumber("test");
        member.setBirth("test");
        member.setPoint(0);
        member.setCreatedAt(LocalDateTime.now());
        memberRepository.save(member);
    }


}
