package com.apple.sobok.member;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;



@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @GetMapping("/user/create")
    public String create() {
        return "signup.html";
    }

    @PostMapping("/user/create")
    public void createMember(@RequestBody MemberDto memberDto) {
        Member member = new Member();
        member.setUsername(memberDto.getUsername());
        member.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 비밀번호 암호화
        member.setName(memberDto.getName());
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
        System.out.println("회원가입 완료");


    }

    @GetMapping("/user/login")
    public String login() {
        return "login.html";
    }


    @PostMapping("/user/login/jwt")
    public String loginJWT(@RequestBody MemberLoginDto memberLoginDto) {
        var authToken = new UsernamePasswordAuthenticationToken(memberLoginDto.getUsername(), memberLoginDto.getPassword());

        var auth = authenticationManagerBuilder.getObject().authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(auth); // 인증 정보 저장

        var jwt = JwtUtil.createToken(SecurityContextHolder.getContext().getAuthentication()); // JWT 생성
        System.out.println(jwt);
        return jwt;
    }


}
