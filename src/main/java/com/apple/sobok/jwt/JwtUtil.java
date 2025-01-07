package com.apple.sobok.jwt;

import com.apple.sobok.member.MyUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;

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
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7일
    private final RefreshTokenRepository refreshTokenRepository;

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
            var authorities = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
            //유효기간 10초
            return Jwts.builder()
                    .claim("username", user.getUsername())
                    .claim("displayName", user.displayName)
                    .claim("authorities", authorities)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 3600000)) //유효기간 10초
                    .signWith(key)
                    .compact();
        }
    }

    // JWT Refresh Token 생성 및 refreshToeknRepository에 저장
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
    public Claims extractToken(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }



}
