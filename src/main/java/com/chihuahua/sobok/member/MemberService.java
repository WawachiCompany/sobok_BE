package com.chihuahua.sobok.member;

import com.chihuahua.sobok.account.Account;
import com.chihuahua.sobok.exception.BadRequestException;
import com.chihuahua.sobok.exception.UnauthorizedException;
import com.chihuahua.sobok.jwt.JwtUtil;
import com.chihuahua.sobok.member.point.PointLog;
import com.chihuahua.sobok.member.point.PointLogService;
import com.chihuahua.sobok.member.point.Premium;
import com.chihuahua.sobok.member.point.PremiumRepository;
import com.chihuahua.sobok.member.point.PremiumResponseDto;
import com.chihuahua.sobok.oauth.OauthAccount;
import com.chihuahua.sobok.oauth.OauthAccountRepository;
import com.chihuahua.sobok.routine.todo.Todo;
import com.chihuahua.sobok.routine.todo.TodoDto;
import com.chihuahua.sobok.routine.todo.TodoRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final PremiumRepository premiumRepository;
  private final PointLogService pointLogService;
  private final JwtUtil jwtUtil;
  private final TodoRepository todoRepository;
  private final OauthAccountRepository oauthAccountRepository;
  @Lazy
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManagerBuilder authenticationManagerBuilder;

  public Member createMember(MemberDto memberDto) {
    Member member = new Member();
    member.setUsername(memberDto.getUsername());
    member.setPassword(passwordEncoder.encode(memberDto.getPassword())); // 비밀번호 암호화
    member.setName(memberDto.getName());
    member.setDisplayName(memberDto.getDisplayName());
    member.setEmail(memberDto.getEmail());
    member.setPhoneNumber(memberDto.getPhoneNumber());
    member.setBirth(memberDto.getBirth());
    member.setIsOauth(false);
    return memberRepository.save(member);
  }

  public boolean isEmailDuplicated(String email) {
    return memberRepository.existsByEmail(email);
  }

  public boolean isPhoneNumberDuplicated(String phoneNumber) {
    return memberRepository.existsByPhoneNumber(phoneNumber);
  }

  public boolean isUsernameDuplicated(String username) {
    return memberRepository.existsByUsername(username);
  }

  public boolean isDisplayNameDuplicated(String displayName) {
    return memberRepository.existsByDisplayName(displayName);
  }

  public Optional<Member> findByEmail(String email) {
    return memberRepository.findByEmail(email);
  }

  // MemberController에서 유저 정보 조회
  public Map<String, Object> getUserInfo(Member member) {
    Map<String, Object> response = new HashMap<>();
    response.put("username", member.getUsername());
    response.put("id", member.getId());
    response.put("name", member.getName());
    response.put("displayName", member.getDisplayName());
    response.put("point", member.getPoint());
    response.put("email", member.getEmail());
    response.put("phoneNumber", member.getPhoneNumber());
    response.put("birth", member.getBirth());
    response.put("isPremium", member.getIsPremium());
    response.put("totalAchievedTime", member.getTotalAchievedTime());
    response.put("totalAccountBalance", member.getTotalAccountBalance());
    response.put("weeklyRoutineTime", member.getWeeklyRoutineTime());
    if (member.getIsPremium()) {
      Premium premium = premiumRepository.findByMemberAndEndAtAfter(member, LocalDate.now())
          .orElseThrow(() -> new IllegalArgumentException("프리미엄 정보를 찾을 수 없습니다."));
      response.put("premiumEndAt", premium.getEndAt());
    }
    response.put("message", "유저 정보 조회 성공");
    return response;
  }

  @Transactional
  public Member saveOrUpdate(Member member) {
    Optional<Member> result = memberRepository.findByUsername(member.getUsername());
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

  public Map<String, Object> loginJWT(MemberLoginDto memberLoginDto, HttpServletResponse response) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        memberLoginDto.getUsername(),
        memberLoginDto.getPassword());
    Authentication auth = authenticationManagerBuilder.getObject().authenticate(authToken);
    SecurityContextHolder.getContext().setAuthentication(auth); // 인증 정보 저장

    String jwt = jwtUtil.createToken(
        SecurityContextHolder.getContext().getAuthentication()); // JWT 생성
    String refreshToken = jwtUtil.createRefreshToken(
        SecurityContextHolder.getContext().getAuthentication()); // Refresh Token 생성

    //HttpOnly 쿠키 설정
    setAccessCookie(response, jwt);
    setRefreshCookie(response, refreshToken);

    //Access Token ResponseEntity에 저장
    return memberLoginSuccess(memberLoginDto, jwt);
  }

  public void setAccessCookie(HttpServletResponse response, String accessToken) {
    // HttpOnly 쿠키 설정
    Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(true);
    accessTokenCookie.setPath("/"); // 전체 경로에서 사용 가능
    accessTokenCookie.setMaxAge(15 * 60); // 15분 유효
    response.addCookie(accessTokenCookie);
  }

  public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
    // HttpOnly 쿠키 설정
    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
    refreshTokenCookie.setPath("/"); // 전체 경로에서 사용 가능
    refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 유효
    response.addCookie(refreshTokenCookie);
  }

  // 쿠키 제거
  public void removeCookie(HttpServletResponse response, String cookieName) {
    Cookie cookie = new Cookie(cookieName, null);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0); // 쿠키 제거
    response.addCookie(cookie);
  }

  // member로 로그인 성공 시 응답 바디
  public Map<String, Object> memberLoginSuccess(MemberLoginDto memberLoginDto, String jwt) {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("accessToken", jwt);
    responseBody.put("username", memberLoginDto.getUsername());
    responseBody.put("message", "로그인 성공");
    responseBody.put("status", HttpStatus.OK.value());
    return responseBody;
  }

  public void logout(HttpServletResponse response, HttpServletRequest request) {

    removeCookie(response, "refreshToken");
    removeCookie(response, "accessToken");

    String refreshToken = jwtUtil.extractRefreshTokenFromRequest(request);
    jwtUtil.deleteRefreshToken(refreshToken);
  }

  public Map<String, Object> refreshToken(HttpServletRequest request,
      HttpServletResponse response) {

    String refreshToken = jwtUtil.extractRefreshTokenFromRequest(request);
    if (refreshToken != null) {
      String newAccessToken = jwtUtil.refreshAccessToken(refreshToken, request, response);
      Map<String, Object> result = new HashMap<>();
      result.put("accessToken", newAccessToken);
      result.put("message", "토큰 갱신 성공");
      return result;
    } else {
      throw new BadRequestException("Refresh token이 존재하지 않습니다.");
    }
  }

  public void upgradeToPremium(Member member) {
    Integer point = member.getPoint();
    Integer premiumPrice = member.getPremiumPrice();
    if (point < premiumPrice) {
      throw new IllegalArgumentException("포인트가 부족합니다.");
    }
    PointLog pointLog = new PointLog();
    pointLog.setMember(member);
    pointLog.setPoint(-premiumPrice);
    pointLog.setBalance(point - premiumPrice);
    pointLog.setCategory("구독권 구매");

    member.setPoint(point - premiumPrice);
    member.setIsPremium(true);

    Premium premium = premiumRepository.findByMemberAndEndAtAfter(member, LocalDate.now())
        .orElseGet(() -> {
          Premium newPremium = new Premium();
          newPremium.setMember(member);
          return newPremium;
        });
    premium.setStartAt(LocalDate.now());
    premium.setEndAt(LocalDate.now().plusMonths(1));
//        premium.setIsAutoRenewal(false);
    premiumRepository.save(premium);
    memberRepository.save(member);
    pointLogService.save(pointLog);
  }

  @Transactional
  public Member getMember() {
    String token = jwtUtil.extractAccessTokenFromRequestHeader();
    if (!jwtUtil.validateToken(token)) {
      throw new UnauthorizedException("액세스 토큰이 만료되었습니다.");
    }

    // JWT 토큰에서 클레임 추출
    Claims claims = JwtUtil.extractToken(token);
    String loginType = claims.get("loginType", String.class);
    String provider = claims.get("provider", String.class);

    // Apple 네이티브 로그인인 경우
    if ("native".equals(loginType) && "apple".equals(provider)) {
      String sub = claims.getSubject();
      OauthAccount oauthAccount = oauthAccountRepository.findByOauthIdAndProvider(sub, "apple")
          .orElseThrow(() -> new UnauthorizedException("Apple OAuth 계정을 찾을 수 없습니다."));
      return oauthAccount.getMember();
    }

    // 일반 로그인 및 OAuth 로그인인 경우
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || authentication.getPrincipal() instanceof String) {
      throw new UnauthorizedException("인증 정보가 올바르지 않습니다.");
    }

    MyUserDetailsService.CustomUser customUser = (MyUserDetailsService.CustomUser) authentication.getPrincipal();
    String username = customUser.getUsername();

    return memberRepository.findByUsername(username)
        .orElseThrow(() -> new UnauthorizedException("유저를 찾을 수 없습니다."));
  }

  public Integer calculatePremiumPrice(Member member) {
    List<Account> accounts = member.getAccounts();
    long totalTimeOfAccounts = accounts.stream()
        .mapToLong(Account::getTime)
        .sum();
    return (int) (totalTimeOfAccounts * 0.9);
  }

  public void updateOrSaveLinkApps(Member member, List<String> linkApps) {
    member.setLinkApps(linkApps);
    memberRepository.save(member);
  }

  public Map<String, Object> getLinkApps(Member member) {
    Map<String, Object> response = new HashMap<>();
    response.put("linkApps", member.getLinkApps());
    response.put("message", "연동 앱 조회 성공");
    return response;
  }

  public List<PremiumResponseDto> getPremiumLog(Member member) {
    List<Premium> premiumList = premiumRepository.findByMember(member);
    return premiumList.stream()
        .map(premium -> {
          PremiumResponseDto premiumResponseDto = new PremiumResponseDto();
          premiumResponseDto.setStartAt(premium.getStartAt());
          premiumResponseDto.setEndAt(premium.getEndAt());
          return premiumResponseDto;
        }).collect(Collectors.toList());
  }

  public List<TodoDto> getTodosByLinkApp(Member member, String linkApp) {
    List<Todo> todos = todoRepository.findAllByMemberAndLinkApp(member, linkApp);
    return todos.stream().map(this::convertToDto).collect(Collectors.toList());
  }

  private TodoDto convertToDto(Todo todo) {
    TodoDto todoDto = new TodoDto();
    todoDto.setId(todo.getId());
    todoDto.setTitle(todo.getTitle());
    todoDto.setCategory(todo.getCategory());
    todoDto.setStartTime(todo.getStartTime());
    todoDto.setEndTime(todo.getEndTime());
    todoDto.setLinkApp(todo.getLinkApp());
    todoDto.setRoutineId(todo.getRoutine().getId());
    return todoDto;
  }

  // 회원 정보 수정(전화번호, 이메일, 생년월일)
  public void updateMember(Member member, MemberDto memberDto) {
    if (memberDto.getPhoneNumber() != null) {
      if (isPhoneNumberDuplicated(memberDto.getPhoneNumber())) {
        throw new BadRequestException("이미 사용 중인 전화번호입니다.");
      }
      member.setPhoneNumber(memberDto.getPhoneNumber());
    }
    if (memberDto.getEmail() != null) {
      if (isEmailDuplicated(memberDto.getEmail())) {
        throw new BadRequestException("이미 사용 중인 이메일입니다.");
      }
      member.setEmail(memberDto.getEmail());
    }
    if (memberDto.getBirth() != null) {
      member.setBirth(memberDto.getBirth());
    }
    memberRepository.save(member);
  }

  public boolean updatePassword(Member member, String currentPassword, String newPassword) {
    // 현재 비밀번호 확인
    if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
      return false; // 현재 비밀번호가 일치하지 않음
    }

    // 비밀번호 기준 충족하는지 확인(8~16자, 대문자, 소문자, 숫자, 특수문자 포함)
    if (!newPassword.matches(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$")) {
      throw new BadRequestException("비밀번호는 8~16자, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.");
    }

    // 새 비밀번호 암호화 및 저장
    member.setPassword(passwordEncoder.encode(newPassword));
    memberRepository.save(member);
    return true;
  }

  // 닉네임 수정
  public void updateDisplayName(Member member, String displayName) {
    member.setDisplayName(displayName);
    memberRepository.save(member);
  }


}
