package com.apple.sobok.member;

import com.apple.sobok.jwt.JwtUtil;
import com.apple.sobok.member.point.PremiumResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMember(@RequestBody MemberDto memberDto) {
        try {
            Member member = new Member();
            member.setUsername(memberDto.getUsername());
            member.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 비밀번호 암호화
            member.setName(memberDto.getName());
            member.setDisplayName(memberDto.getDisplayName());
            member.setEmail(memberDto.getEmail());
            member.setPhoneNumber(memberDto.getPhoneNumber());
            member.setBirth(memberDto.getBirth());
            member.setPoint(0);
            member.setCreatedAt(LocalDateTime.now());
            member.setIsOauth(false);
            member.setIsPremium(false);
            member.setConsecutiveAchieveCount(0);
            member.setPremiumPrice(9999);
            member.setTotalAchievedTime(0);
            member.setTotalAccountBalance(0);
            member.setWeeklyRoutineTime(0);
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

    @GetMapping("/is-duplicated/email")
    public ResponseEntity<Map<String, Object>> isEmailDuplicated(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicated", memberService.isEmailDuplicated(email));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/is-duplicated/phone-number")
    public ResponseEntity<Map<String, Object>> isPhoneNumberDuplicated(@RequestParam String phoneNumber) {
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicated", memberService.isPhoneNumberDuplicated(phoneNumber));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/is-duplicated/username")
    public ResponseEntity<Map<String, Object>> isUsernameDuplicated(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicated", memberService.isUsernameDuplicated(username));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/is-duplicated/display-name")
    public ResponseEntity<Map<String, Object>> isDisplayNameDuplicated(@RequestParam String displayName) {
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicated", memberService.isDisplayNameDuplicated(displayName));
        return ResponseEntity.ok(response);
    }


    @GetMapping("/login")
    public String login() {
        return "login.html";
    }


    @PostMapping("/login/jwt")
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




    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        try {
            Member member = memberService.getMember();
            Map<String, Object> response = memberService.getUserInfo(member);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("message", "유저 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            memberService.removeCookie(response, "refreshToken");
            memberService.removeCookie(response, "accessToken");

            String refreshToken = jwtUtil.extractRefreshTokenFromRequest(request);
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

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = jwtUtil.extractRefreshTokenFromRequest(request);
            if (refreshToken != null) {
                String newAccessToken = jwtUtil.refreshAccessToken(refreshToken, request, response);
                Map<String, Object> result = new HashMap<>();
                result.put("accessToken", newAccessToken);
                result.put("message", "토큰 갱신 성공");
                return ResponseEntity.ok(result);
            } else {
                throw new IllegalArgumentException("No refresh token found");
            }
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("message", "토큰 갱신 실패: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }

    @PutMapping("/premium")
    public ResponseEntity<?> upgradeToPremium() {
        try {
            Member member = memberService.getMember();
            memberService.upgradeToPremium(member);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "구독권 등록 성공");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "구독권 등록 실패: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/achieve")
    public ResponseEntity<?> getAchieveCount() {
        try {
            Member member = memberService.getMember();
            Map<String, Object> response = new HashMap<>();
            response.put("achieveCount", member.getConsecutiveAchieveCount());
            response.put("message", "연속 달성일 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "연속 달성일 조회 실패: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/premium")
    public ResponseEntity<?> getPremiumPrice() {
        try {
            Member member = memberService.getMember();
            Map<String, Object> response = new HashMap<>();
            response.put("price", member.getPremiumPrice());
            response.put("message", "구독권 가격 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "구독권 가격 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/link-app")
    public ResponseEntity<?> linkApp(@RequestBody Map<String, List<String>> linkApps) {
        try {
            Member member = memberService.getMember();
            memberService.updateOrSaveLinkApps(member, linkApps.get("linkApps"));
            Map<String, Object> response = new HashMap<>();
            response.put("message", "연동 앱 등록 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "연동 앱 등록 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/link-app")
    public ResponseEntity<?> getLinkApps() {
        try {
            Member member = memberService.getMember();
            Map<String, Object> response = memberService.getLinkApps(member);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "연동 앱 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/premium/log")
    public ResponseEntity<?> getPremiumLog() {
        try {
            Member member = memberService.getMember();
            List<PremiumResponseDto> result = memberService.getPremiumLog(member);
            Map<String, Object> response = new HashMap<>();
            response.put("premiumLog", result);
            response.put("message", "프리미엄 로그 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "프리미엄 로그 조회 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
