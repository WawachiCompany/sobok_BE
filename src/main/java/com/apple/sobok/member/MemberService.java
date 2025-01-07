package com.apple.sobok.member;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    public void setCookie(HttpServletResponse response, String refreshToken) {
        // HttpOnly 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
//            refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
        refreshTokenCookie.setPath("/"); // 전체 경로에서 사용 가능
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 유효
        response.addCookie(refreshTokenCookie);

        System.out.println("refreshTokenCookie = " + refreshTokenCookie);
    }

    // member로 로그인 성공 시 응답 바디
    public Map<String, Object> memberLoginSuccess(MemberLoginDto memberLoginDto, String jwt) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("accessToken", jwt);
        responseBody.put("username", memberLoginDto.getUsername()); //리턴으로 보내줄 거
        responseBody.put("message", "로그인 성공");
        responseBody.put("timestamp", LocalDateTime.now());
        responseBody.put("status", HttpStatus.OK.value());
        return responseBody;
    }


}


