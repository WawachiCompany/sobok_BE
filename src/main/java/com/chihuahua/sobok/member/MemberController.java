package com.chihuahua.sobok.member;

import com.chihuahua.sobok.exception.BadRequestException;
import com.chihuahua.sobok.member.point.PremiumResponseDto;
import com.chihuahua.sobok.routine.todo.TodoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class MemberController {

  private final MemberService memberService;

  @PostMapping("/create")
  public ResponseEntity<Map<String, Object>> createMember(@RequestBody MemberDto memberDto) {

    Member createdMember = memberService.createMember(memberDto);

    Map<String, Object> response = new HashMap<>();
    response.put("id", createdMember.getId());
    response.put("username", createdMember.getUsername());
    response.put("email", createdMember.getEmail());
    response.put("message", "회원가입 완료");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/is-duplicated/email")
  public ResponseEntity<Map<String, Object>> isEmailDuplicated(@RequestParam String email) {
    Map<String, Object> response = new HashMap<>();
    response.put("isDuplicated", memberService.isEmailDuplicated(email));
    return ResponseEntity.ok(response);
  }

  @GetMapping("/is-duplicated/phone-number")
  public ResponseEntity<Map<String, Object>> isPhoneNumberDuplicated(
      @RequestParam String phoneNumber) {
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
  public ResponseEntity<Map<String, Object>> isDisplayNameDuplicated(
      @RequestParam String displayName) {
    Map<String, Object> response = new HashMap<>();
    response.put("isDuplicated", memberService.isDisplayNameDuplicated(displayName));
    return ResponseEntity.ok(response);
  }


  @GetMapping("/login")
  public String login() {
    return "login.html";
  }


  @PostMapping("/login/jwt")
  public ResponseEntity<Map<String, Object>> loginJWT(@RequestBody MemberLoginDto memberLoginDto,
      HttpServletResponse response) {
    Map<String, Object> responseBody = memberService.loginJWT(memberLoginDto, response);
    return ResponseEntity.ok(responseBody); // JSON 형식으로 응답 반환
  }

  @PutMapping("/update/general")
  public ResponseEntity<Map<String, Object>> updateMember(@RequestBody MemberDto memberDto) {
    Member member = memberService.getMember();
    memberService.updateMember(member, memberDto);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "회원 정보 수정 성공");
    return ResponseEntity.ok(response);
  }

  @PutMapping("/update/password")
  public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody PasswordDto passwordDto) {
    Member member = memberService.getMember();
    boolean isChanged = memberService.updatePassword(
        member,
        passwordDto.getOldPassword(),
        passwordDto.getNewPassword());
    if (!isChanged) {
      throw new BadRequestException("비밀번호 수정 실패: 비밀번호가 일치하지 않습니다.");
    }
    Map<String, Object> response = new HashMap<>();
    response.put("message", "비밀번호 수정 성공");
    return ResponseEntity.ok(response);
  }

  @PutMapping("/update/display-name")
  public ResponseEntity<?> updateDisplayName(@RequestParam String displayName) {
    Member member = memberService.getMember();
    memberService.updateDisplayName(member, displayName);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "닉네임 수정 성공");
    return ResponseEntity.ok(response);
  }

  // Apple, 카카오 OAuth 회원의 추가 정보 입력
  @PutMapping("/oauth/additional-info")
  public ResponseEntity<Map<String, Object>> updateOauthAdditionalInfo(
      @RequestBody OauthAdditionalInfoDto additionalInfoDto) {
    Member member = memberService.getMember();
    memberService.updateOauthAdditionalInfo(member, additionalInfoDto);

    Map<String, Object> response = new HashMap<>();
    response.put("message", "추가 정보 입력 완료");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> info() {
    Member member = memberService.getMember();
    Map<String, Object> response = memberService.getUserInfo(member);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response,
      HttpServletRequest request) {
    memberService.logout(response, request);
    Map<String, Object> responseMap = new HashMap<>();
    responseMap.put("message", "로그아웃 성공");
    return ResponseEntity.ok(responseMap);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request,
      HttpServletResponse response) {
    Map<String, Object> result = memberService.refreshToken(request, response);
    return ResponseEntity.ok(result);
  }

  @PutMapping("/premium")
  public ResponseEntity<?> upgradeToPremium() {
    Member member = memberService.getMember();
    memberService.upgradeToPremium(member);
    Map<String, Object> response = new HashMap<>();
    response.put("message", "구독권 등록 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/achieve")
  public ResponseEntity<?> getAchieveCount() {
    Member member = memberService.getMember();
    Map<String, Object> response = new HashMap<>();
    response.put("achieveCount", member.getConsecutiveAchieveCount());
    response.put("message", "연속 달성일 조회 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/premium")
  public ResponseEntity<?> getPremiumPrice() {
    Member member = memberService.getMember();
    Map<String, Object> response = new HashMap<>();
    response.put("price", member.getPremiumPrice());
    response.put("message", "구독권 가격 조회 성공");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/link-app")
  public ResponseEntity<?> linkApp(@RequestBody Map<String, List<String>> linkApps) {
    Member member = memberService.getMember();
    memberService.updateOrSaveLinkApps(member, linkApps.get("linkApps"));
    Map<String, Object> response = new HashMap<>();
    response.put("message", "연동 앱 등록 성공");
    return ResponseEntity.ok(response);
  }

  @GetMapping("/link-app")
  public ResponseEntity<?> getLinkApps() {
    Member member = memberService.getMember();
    Map<String, Object> response = memberService.getLinkApps(member);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/link-app/todos")
  public ResponseEntity<?> getLinkAppTodos(@RequestParam String linkApp) {
    Member member = memberService.getMember();
    List<TodoDto> todos = memberService.getTodosByLinkApp(member, linkApp);
    Map<String, Object> response = new HashMap<>();
    response.put("todos", todos);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/premium/log")
  public ResponseEntity<?> getPremiumLog() {
    Member member = memberService.getMember();
    List<PremiumResponseDto> result = memberService.getPremiumLog(member);
    Map<String, Object> response = new HashMap<>();
    response.put("premiumLog", result);
    response.put("message", "프리미엄 로그 조회 성공");
    return ResponseEntity.ok(response);
  }

}
