package com.apple.sobok.member;

import com.apple.sobok.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;

    @GetMapping("/user/create")
    public String create() {
        return "signup.html";
    }

    @PostMapping("/user/create")
    public ResponseEntity<Map<String, Object>> createMember(@RequestBody MemberDto memberDto) {
        try {
            Member member = new Member();
            member.setUsername(memberDto.getUsername());
            member.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 비밀번호 암호화
            member.setName(memberDto.getName());
            member.setDisplayName(memberDto.getDisplayName());

            if (memberService.isEmailDuplicated(memberDto.getEmail())) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
            } else {
                member.setEmail(memberDto.getEmail());
            }

            if (memberService.isPhoneNumberDuplicated(memberDto.getPhoneNumber())) {
                throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
            } else {
                member.setPhoneNumber(memberDto.getPhoneNumber());
            }

            member.setBirth(memberDto.getBirth());
            member.setPoint(0);
            member.setCreatedAt(LocalDateTime.now());
            member.setIsOauth(false);
            memberRepository.save(member);

            Map<String, Object> response = new HashMap<>();
            response.put("id", member.getId());
            response.put("username", member.getUsername());
            response.put("email", member.getEmail());
            response.put("message", "회원가입 완료");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "회원가입 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @GetMapping("/user/login")
    public String login() {
        return "login.html";
    }


    @PostMapping("/user/login/jwt")
    public ResponseEntity<Map<String, Object>> loginJWT(@RequestBody MemberLoginDto memberLoginDto, HttpServletResponse response) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    memberLoginDto.getUsername(),
                    memberLoginDto.getPassword());
            var auth = authenticationManagerBuilder.getObject().authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth); // 인증 정보 저장

            var jwt = jwtUtil.createToken(SecurityContextHolder.getContext().getAuthentication()); // JWT 생성
            var refreshToken = jwtUtil.createRefreshToken(SecurityContextHolder.getContext().getAuthentication()); // Refresh Token 생성

            //HttpOnly 쿠키 설정
            memberService.setAccessCookie(response, jwt);
            memberService.setRefreshCookie(response, refreshToken);

            //Access Token ResponseEntity에 저장
            Map<String, Object> responseBody = memberService.memberLoginSuccess(memberLoginDto, jwt);

            return ResponseEntity.ok(responseBody); // JSON 형식으로 응답 반환

        } catch (Exception e) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("timestamp", LocalDateTime.now());
            responseBody.put("message", "로그인 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
        }
    }




    @GetMapping("/user/info")
    public ResponseEntity<Map<String, Object>> info() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
        try {
            Map<String, Object> response = memberService.getUserInfo(user);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "유저 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            memberService.removeCookie(response, "refreshToken");
            memberService.removeCookie(response, "accessToken");

            String refreshToken = jwtUtil.extractTokenFromRequest(request);
            System.out.println("refreshToken = " + refreshToken);
            jwtUtil.deleteRefreshToken(refreshToken);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "로그아웃 성공");
            responseMap.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(responseMap);

        } catch (Exception e) {
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("message", "로그아웃 실패: " + e.getMessage());
            responseMap.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
        }
    }

    @PostMapping("/user/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = jwtUtil.extractTokenFromRequest(request);
            if (refreshToken != null) {
                String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);
                Map<String, Object> response = new HashMap<>();
                response.put("accessToken", newAccessToken);
                response.put("message", "토큰 갱신 성공");
                return ResponseEntity.ok(response);
            } else {
                throw new IllegalArgumentException("No refresh token found");
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "토큰 갱신 실패: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


}
