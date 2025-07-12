package com.chihuahua.sobok.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleTokenVerifier {

  @Value("${social-login.provider.apple.client-id}")
  private String appleClientId;

  private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

  public AppleUserInfo verifyIdentityToken(String identityToken) throws Exception {
    try {
      // 1. JWT 파싱
      SignedJWT signedJWT = SignedJWT.parse(identityToken);
      JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

      // 2. 기본 검증 (만료시간, 발급자 등)
      validateBasicClaims(claimsSet);

      // 3. Apple 공개키로 서명 검증
      verifySignature(signedJWT);

      // 4. 사용자 정보 추출
      return extractUserInfo(claimsSet);

    } catch (ParseException e) {
      throw new RuntimeException("JWT 파싱 실패", e);
    }
  }

  private void validateBasicClaims(JWTClaimsSet claimsSet) {
    // 만료시간 검증
    Date expirationTime = claimsSet.getExpirationTime();
    if (expirationTime == null || expirationTime.before(new Date())) {
      throw new RuntimeException("토큰이 만료되었습니다");
    }

    // 발급자 검증
    String issuer = claimsSet.getIssuer();
    if (!"https://appleid.apple.com".equals(issuer)) {
      throw new RuntimeException("잘못된 발급자입니다");
    }

    // 대상 검증 (클라이언트 ID)
    String audience = claimsSet.getAudience().getFirst();
    if (!appleClientId.equals(audience)) {
      throw new RuntimeException("잘못된 대상입니다");
    }
  }

  private void verifySignature(SignedJWT signedJWT) throws Exception {
    try {
      // Apple 공개키 가져오기
      JWKSet jwkSet = JWKSet.load(new URI(APPLE_KEYS_URL).toURL());
      String keyId = signedJWT.getHeader().getKeyID();

      JWK jwk = jwkSet.getKeyByKeyId(keyId);
      if (jwk == null) {
        throw new RuntimeException("해당 키 ID를 찾을 수 없습니다: " + keyId);
      }

      RSAKey rsaKey = (RSAKey) jwk;
      JWSVerifier verifier = new RSASSAVerifier(rsaKey);

      if (!signedJWT.verify(verifier)) {
        throw new RuntimeException("JWT 서명 검증 실패");
      }

    } catch (JOSEException e) {
      throw new RuntimeException("JWT 서명 검증 중 오류 발생", e);
    }
  }

  private AppleUserInfo extractUserInfo(JWTClaimsSet claimsSet) throws ParseException {
    String sub = claimsSet.getSubject();
    String email = claimsSet.getStringClaim("email");
    Boolean emailVerified = claimsSet.getBooleanClaim("email_verified");

    AppleUserInfo userInfo = new AppleUserInfo();
    userInfo.setSub(sub);
    userInfo.setEmail(email);
    userInfo.setEmailVerified(emailVerified != null ? emailVerified : false);

    return userInfo;
  }
}
