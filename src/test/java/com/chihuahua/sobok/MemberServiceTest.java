package com.chihuahua.sobok;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberDto;
import com.chihuahua.sobok.member.MemberRepository;
import com.chihuahua.sobok.member.MemberService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private MemberService memberService;

  private Member testMember;
  private MemberDto testMemberDto;

  @BeforeEach
  void setUp() {
    testMember = new Member();
    testMember.setId(1L);
    testMember.setEmail("test@example.com");
    testMember.setUsername("testuser");
    testMember.setDisplayName("Test User");
    testMember.setName("김테스트");
    testMember.setPhoneNumber("010-1234-5678");
    testMember.setBirth("1990-01-01");
    testMember.setPoint(1000);
    testMember.setTotalAccountBalance(0);
    testMember.setTotalAchievedTime(0);
    testMember.setPremiumPrice(9999);
    testMember.setIsPremium(false);
    testMember.setIsOauth(false);

    testMemberDto = new MemberDto();
    testMemberDto.setEmail("test@example.com");
    testMemberDto.setUsername("testuser");
    testMemberDto.setDisplayName("Test User");
    testMemberDto.setName("김테스트");
    testMemberDto.setPhoneNumber("010-1234-5678");
    testMemberDto.setBirth("1990-01-01");
    testMemberDto.setPassword("password123");
  }

  @Test
  @DisplayName("회원 생성 테스트")
  void testCreateMember() {
    // Given
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(memberRepository.save(any(Member.class))).thenReturn(testMember);

    // When
    Member savedMember = memberService.createMember(testMemberDto);

    // Then
    assertThat(savedMember).isNotNull();
    assertThat(savedMember.getEmail()).isEqualTo("test@example.com");
    assertThat(savedMember.getUsername()).isEqualTo("testuser");
    assertThat(savedMember.getDisplayName()).isEqualTo("Test User");
    assertThat(savedMember.getIsOauth()).isFalse();
  }

  @Test
  @DisplayName("이메일로 회원 조회 테스트")
  void testFindByEmail() {
    // Given
    when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));

    // When
    Optional<Member> foundMember = memberService.findByEmail("test@example.com");

    // Then
    assertThat(foundMember).isPresent();
    assertThat(foundMember.get().getEmail()).isEqualTo("test@example.com");
    assertThat(foundMember.get().getId()).isEqualTo(testMember.getId());
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 회원 조회 테스트")
  void testFindByEmailNotFound() {
    // Given
    when(memberRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // When
    Optional<Member> foundMember = memberService.findByEmail("nonexistent@example.com");

    // Then
    assertThat(foundMember).isEmpty();
  }

  @Test
  @DisplayName("이메일 중복 확인 테스트")
  void testIsEmailDuplicated() {
    // Given
    when(memberRepository.existsByEmail("test@example.com")).thenReturn(true);
    when(memberRepository.existsByEmail("new@example.com")).thenReturn(false);

    // When & Then
    assertThat(memberService.isEmailDuplicated("test@example.com")).isTrue();
    assertThat(memberService.isEmailDuplicated("new@example.com")).isFalse();
  }

  @Test
  @DisplayName("사용자명 중복 확인 테스트")
  void testIsUsernameDuplicated() {
    // Given
    when(memberRepository.existsByUsername("testuser")).thenReturn(true);
    when(memberRepository.existsByUsername("newuser")).thenReturn(false);

    // When & Then
    assertThat(memberService.isUsernameDuplicated("testuser")).isTrue();
    assertThat(memberService.isUsernameDuplicated("newuser")).isFalse();
  }

  @Test
  @DisplayName("전화번호 중복 확인 테스트")
  void testIsPhoneNumberDuplicated() {
    // Given
    when(memberRepository.existsByPhoneNumber("010-1234-5678")).thenReturn(true);
    when(memberRepository.existsByPhoneNumber("010-9999-9999")).thenReturn(false);

    // When & Then
    assertThat(memberService.isPhoneNumberDuplicated("010-1234-5678")).isTrue();
    assertThat(memberService.isPhoneNumberDuplicated("010-9999-9999")).isFalse();
  }

  @Test
  @DisplayName("닉네임 중복 확인 테스트")
  void testIsDisplayNameDuplicated() {
    // Given
    when(memberRepository.existsByDisplayName("Test User")).thenReturn(true);
    when(memberRepository.existsByDisplayName("New User")).thenReturn(false);

    // When & Then
    assertThat(memberService.isDisplayNameDuplicated("Test User")).isTrue();
    assertThat(memberService.isDisplayNameDuplicated("New User")).isFalse();
  }

  @Test
  @DisplayName("회원 정보 조회 테스트")
  void testGetUserInfo() {
    // When
    Map<String, Object> userInfo = memberService.getUserInfo(testMember);

    // Then
    assertThat(userInfo).isNotNull();
    assertThat(userInfo.get("username")).isEqualTo("testuser");
    assertThat(userInfo.get("email")).isEqualTo("test@example.com");
    assertThat(userInfo.get("displayName")).isEqualTo("Test User");
    assertThat(userInfo.get("point")).isEqualTo(1000);
    assertThat(userInfo.get("isPremium")).isEqualTo(false);
    assertThat(userInfo.get("message")).isEqualTo("유저 정보 조회 성공");
  }

  @Test
  @DisplayName("프리미엄 가격 계산 테스트 - 계정이 없는 경우")
  void testCalculatePremiumPriceWithNoAccounts() {
    // Given
    testMember.setAccounts(null);

    // When
    Integer premiumPrice = memberService.calculatePremiumPrice(testMember);

    // Then
    assertThat(premiumPrice).isEqualTo(9999); // 기본 프리미엄 가격
  }
}
