package com.apple.sobok.jwt;

import com.apple.sobok.member.MyUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    static final SecretKey key =
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                    "jwtpassword123jwtpassword123jwtpassword123jwtpassword123jwtpassword"
            ));
    private static final long ACCESS_TOKEN_EXPIRATION = 900_000; // 15분
    private static final long REFRESH_TOKEN_EXPIRATION = 30 * 24 * 60 * 60 * 1000L; // 30일
    private final RefreshTokenRepository refreshTokenRepository;

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        if (validateToken(refreshToken)) {
            Claims claims = extractToken(refreshToken);
            String username = claims.get("username", String.class);
            // 새로운 액세스 토큰 생성
            return createToken(username);
        } else {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    // JWT 만들어주는 함수 (username을 인자로 받는 오버로드 메서드)
    public String createToken(String username) {
        return Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key)
                .compact();
    }

    // JWT 만들어주는 함수
    public String createToken(Authentication auth) {
        if(auth.getPrincipal() instanceof DefaultOidcUser user) {
            //Oauth_id 고유 식별자
            return Jwts.builder()
                    .claim("username", user.getName()) //Oauth_id 고유 식별자
                    .claim("displayName", user.getAttributes().get("name"))
                    .claim("authorities", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)) //유효기간 15분
                    .signWith(key)
                    .compact();
        }
        else {
            var user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
            System.out.println("jwt에서의 auth:" + user.toString());
            var authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
            return Jwts.builder()
                    .claim("username", user.getUsername())
                    .claim("displayName", user.displayName)
                    .claim("authorities", authorities)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION)) //유효기간 10초
                    .signWith(key)
                    .compact();
        }
    }

    // JWT Refresh Token 생성 및 refreshTokenRepository에 저장
    public String createRefreshToken(Authentication auth) {
        if(auth.getPrincipal() instanceof DefaultOidcUser user) {
            var jwt = Jwts.builder()
                                .claim("username", user.getName()) //Oauth_id 고유 식별자
                                .issuedAt(new Date(System.currentTimeMillis()))
                                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                                .signWith(key)
                                .compact();
            //refreshTokenRepository에 저장
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setRefreshToken(jwt);
            refreshToken.setUsername(user.getName());
            refreshToken.setExpiredAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION));
            refreshTokenRepository.save(refreshToken);
            return jwt;
        }
        else {
            var user = (MyUserDetailsService.CustomUser) auth.getPrincipal();
            var jwt = Jwts.builder()
                    .claim("username", user.getUsername())
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
        if(attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return token.substring(7);
            }
        }
        return null;
    }
}
