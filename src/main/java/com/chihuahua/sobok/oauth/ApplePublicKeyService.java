package com.chihuahua.sobok.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ApplePublicKeyService {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String appleClientId;

    /**
     * Apple ID 토큰을 검증하고, 토큰의 클레임을 반환합니다.
     *
     * @param idToken 클라이언트로부터 전달받은 Apple ID 토큰(JWT)
     * @return 검증된 토큰의 클레임(JSON 객체)
     * @throws ParseException, IOException, JOSEException 검증 실패 시 예외 발생
     */
    public Map<String, Object> verifyToken(String idToken) throws ParseException, IOException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(idToken);

        // Apple의 공개 키들을 가져옵니다.
        JWKSet jwkSet;
        try {
            jwkSet = JWKSet.load(new URI(APPLE_KEYS_URL).toURL());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL syntax: " + APPLE_KEYS_URL, e);
        }

        List<JWK> keys = jwkSet.getKeys();
        String kid = signedJWT.getHeader().getKeyID();
        JWK matchingKey = keys.stream()
                .filter(key -> key.getKeyID().equals(kid))
                .findFirst()
                .orElseThrow(() -> new JOSEException("No matching key found for kid: " + kid));

        // 3. 서명 검증
        JWSVerifier verifier = new RSASSAVerifier(matchingKey.toRSAKey());
        if (!signedJWT.verify(verifier)) {
            throw new JOSEException("Apple ID token signature verification failed");
        }

        // 4. 클레임 검증
        Map<String, Object> claims = signedJWT.getJWTClaimsSet().toJSONObject();

        // iss 검증
        String issuer = (String) claims.get("iss");
        if (!"https://appleid.apple.com".equals(issuer)) {
            throw new JOSEException("Invalid issuer: " + issuer);
        }

        // aud 검증: 클라이언트 ID와 일치하는지 확인 (aud는 String 또는 List일 수 있음)
        Object aud = claims.get("aud");
        if (aud instanceof String) {
            if (!appleClientId.equals(aud)) {
                throw new JOSEException("Audience mismatch");
            }
        } else if (aud instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> audList = (List<String>) aud;
            if (!audList.contains(appleClientId)) {
                throw new JOSEException("Audience mismatch");
            }
        }

        // exp 검증
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expirationTime == null || new Date().after(expirationTime)) {
            throw new JOSEException("Token expired");
        }


        return claims;
    }
}
