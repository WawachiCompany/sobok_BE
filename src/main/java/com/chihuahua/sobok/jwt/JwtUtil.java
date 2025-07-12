package com.chihuahua.sobok.jwt;


import com.chihuahua.sobok.member.MyUserDetailsService;
import com.chihuahua.sobok.oauth.AppleUserInfo;
import com.chihuahua.sobok.oauth.GoogleUserInfo;
import com.chihuahua.sobok.oauth.KakaoUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class JwtUtil {

  static final SecretKey key =
      Keys.hmacShaKeyFor(Decoders.BASE64.decode(
          "jwtpassword123jwtpassword123jwtpassword123jwtpassword123jwtpassword"
      ));
  private static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 24 * 1000L; // 1일(배포 시 수정!)
  private static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L; // 30일
  private final RefreshTokenRepository refreshTokenRepository;
  private final SecurityContextService securityContextService;

  // 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
  @Transactional
  public String refreshAccessToken(String refreshToken, HttpServletRequest request,
      HttpServletResponse response) {
    if (validateToken(refreshToken)) {
      Claims claims = extractToken(refreshToken);
      String loginType = claims.get("loginType", String.class);
      String provider = claims.get("provider", String.class);

      // 소셜 로그인인 경우
      if ("native".equals(loginType)) {
        if ("apple".equals(provider)) {
          // Apple 로그인용 새 액세스 토큰 생성
          AppleUserInfo userInfo = new AppleUserInfo();
          userInfo.setSub(claims.getSubject());
          userInfo.setEmail(claims.get("email", String.class));
          return createTokenForAppleUser(userInfo);
        } else if ("kakao".equals(provider)) {
          // 카카오 로그인용 새 액세스 토큰 생성
          KakaoUserInfo userInfo = new KakaoUserInfo();
          userInfo.setId(claims.getSubject());
          userInfo.setEmail(claims.get("email", String.class));
          userInfo.setNickname(claims.get("nickname", String.class));
          return createTokenForKakaoUser(userInfo);
        } else if ("google".equals(provider)) {
          // 구글 로그인용 새 액세스 토큰 생성
          GoogleUserInfo userInfo = new GoogleUserInfo();
          userInfo.setSub(claims.getSubject());
          userInfo.setEmail(claims.get("email", String.class));
          userInfo.setName(claims.get("name", String.class));
          return createTokenForGoogleUser(userInfo);
        }
      }

      // 일반 로그인인 경우
      String username = claims.get("username", String.class);
      // 새로운 액세스 토큰 생성
      String newAccessToken = createToken(username);

      // CustomUser에 필요한 정보 추출
      List<SimpleGrantedAuthority> authorities = new ArrayList<>();
      authorities.add(new SimpleGrantedAuthority("일반유저"));

      UserDetails userDetails = new MyUserDetailsService.CustomUser(username, "", authorities);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      // SecurityContextHolder를 즉시 업데이트
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);

      // SecurityContext 저장
      securityContextService.saveSecurityContext(request, response);

      return newAccessToken;
    } else {
      throw new IllegalArgumentException("Invalid refresh token");
    }
  }

  // JWT 만들어주는 함수 (username을 인자로 받는 오버로드 메서드) -> Refresh Token을 통한 토큰 갱신 시 사용
  public String createToken(String username) {
    return Jwts.builder()
        .claim("username", username)
        .claim("loginType", "normal")
        .claim("provider", "local")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
  }

  // JWT 만들어주는 함수
  public String createToken(Authentication auth) {
    var user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
    return Jwts.builder()
        .claim("username", user.getUsername())
        .claim("displayName", user.displayName)
        .claim("loginType", "normal")
        .claim("provider", "local")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
  }

  // JWT Refresh Token 생성 및 refreshTokenRepository에 저장
  public String createRefreshToken(Authentication auth) {
    var user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
    var jwt = Jwts.builder()
        .claim("username", user.getUsername())
        .claim("loginType", "normal")
        .claim("provider", "local")
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
        .signWith(key)
        .compact();
    //refreshTokenRepository에 저장
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRefreshToken(jwt);
    refreshToken.setUsername(user.getUsername());
    refreshToken.setExpiredAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION));
    refreshTokenRepository.save(refreshToken);
    return jwt;
  }

  // Apple 네이티브 로그인용 토큰 생성
  public String createTokenForAppleUser(AppleUserInfo userInfo) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

    return Jwts.builder()
        .subject(userInfo.getSub())
        .claim("email", userInfo.getEmail())
        .claim("provider", "apple")
        .claim("loginType", "native")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }

  public String createRefreshTokenForAppleUser(AppleUserInfo userInfo) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

    String jwt = Jwts.builder()
        .subject(userInfo.getSub())
        .claim("email", userInfo.getEmail())
        .claim("provider", "apple")
        .claim("loginType", "native")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();

    // RefreshToken DB에 저장
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRefreshToken(jwt);
    refreshToken.setUsername(userInfo.getSub()); // Apple sub를 username으로 사용
    refreshToken.setExpiredAt(expiration);
    refreshTokenRepository.save(refreshToken);

    return jwt;
  }

  // 카카오 네이티브 로그인용 토큰 생성
  public String createTokenForKakaoUser(KakaoUserInfo userInfo) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

    return Jwts.builder()
        .subject(userInfo.getId())
        .claim("email", userInfo.getEmail())
        .claim("nickname", userInfo.getNickname())
        .claim("provider", "kakao")
        .claim("loginType", "native")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }

  public String createRefreshTokenForKakaoUser(KakaoUserInfo userInfo) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

    String jwt = Jwts.builder()
        .subject(userInfo.getId())
        .claim("email", userInfo.getEmail())
        .claim("nickname", userInfo.getNickname())
        .claim("provider", "kakao")
        .claim("loginType", "native")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();

    // RefreshToken DB에 저장
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRefreshToken(jwt);
    refreshToken.setUsername(userInfo.getId()); // 카카오 ID를 username으로 사용
    refreshToken.setExpiredAt(expiration);
    refreshTokenRepository.save(refreshToken);

    return jwt;
  }

  // 구글 네이티브 로그인용 토큰 생성
  public String createTokenForGoogleUser(GoogleUserInfo userInfo) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

    return Jwts.builder()
        .subject(userInfo.getSub())
        .claim("email", userInfo.getEmail())
        .claim("name", userInfo.getName())
        .claim("provider", "google")
        .claim("loginType", "native")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();
  }

  public String createRefreshTokenForGoogleUser(GoogleUserInfo userInfo) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

    String jwt = Jwts.builder()
        .subject(userInfo.getSub())
        .claim("email", userInfo.getEmail())
        .claim("name", userInfo.getName())
        .claim("provider", "google")
        .claim("loginType", "native")
        .issuedAt(now)
        .expiration(expiration)
        .signWith(key)
        .compact();

    // RefreshToken DB에 저장
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setRefreshToken(jwt);
    refreshToken.setUsername(userInfo.getSub()); // 구글 sub를 username으로 사용
    refreshToken.setExpiredAt(expiration);
    refreshTokenRepository.save(refreshToken);

    return jwt;
  }

  // JWT Token 유효성 검사 함수
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return true; // 유효한 경우
    } catch (Exception e) {
      return false; // 유효하지 않은 경우
    }
  }

  // JWT 까주는 함수
  public static Claims extractToken(String token) {
    return Jwts.parser().verifyWith(key).build()
        .parseSignedClaims(token).getPayload();
  }

  // Refresh Token DB에서 삭제
  @Transactional
  public void deleteRefreshToken(String refreshToken) {
    refreshTokenRepository.deleteByRefreshToken(refreshToken);
  }

  // Request에서 Refresh Token 추출
  public String extractRefreshTokenFromRequest(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refreshToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  // Request에서 Refresh Token 추출

  public String extractAccessTokenFromRequestHeader() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs != null) {
      HttpServletRequest request = attrs.getRequest();
      String token = request.getHeader("Authorization");
      if (token != null && token.startsWith("Bearer ")) {
        return token.substring(7);
      }
    }
    return null;
  }
}
