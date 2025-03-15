package com.chihuahua.sobok.oauth;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
public class ApplePublicKeyService {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    @Value("${social-login.provider.apple.client-id}")
    private String appleClientId;

    /**
     * Apple ID 토큰을 검증하고, 토큰의 클레임을 반환합니다.
     *
     * @param idToken 클라이언트로부터 전달받은 Apple ID 토큰(JWT)
     * @return 검증된 토큰의 클레임(JSON 객체)
     * @throws ParseException, IOException, JOSEException 검증 실패 시 예외 발생
     */
    public JSONObject verifyToken(String idToken) throws ParseException, IOException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(idToken);

        // Apple의 공개 키들을 가져옵니다.
        JWKSet jwkSet;
        try {
            jwkSet = JWKSet.load(new URI(APPLE_KEYS_URL).toURL());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL syntax: " + APPLE_KEYS_URL, e);
        }
        List<JWK> keys = jwkSet.getKeys();

        boolean verified = false;
        for (JWK key : keys) {
            if (key.getAlgorithm() != null && key.getAlgorithm().equals(JWSAlgorithm.RS256)) {
                JWSVerifier verifier = new RSASSAVerifier(key.toRSAKey());
                if (signedJWT.verify(verifier)) {
                    verified = true;
                    break;
                }
            }
        }
        if (!verified) {
            throw new JOSEException("Apple ID token signature verification failed");
        }

        // 클레임 추출
        JSONObject claims = new JSONObject(signedJWT.getJWTClaimsSet().toJSONObject());

        // iss 검증: issuer가 반드시 "https://appleid.apple.com"이어야 함
        String issuer = (String) claims.get("iss");
        if (!"https://appleid.apple.com".equals(issuer)) {
            throw new JOSEException("Invalid issuer: " + issuer);
        }

        // aud 검증: 토큰의 audience가 설정한 client-id와 일치해야 함
        Object audObj = claims.get("aud");
        if (audObj instanceof String) {
            if (!appleClientId.equals(audObj)) {
                throw new JOSEException("Audience mismatch");
            }
        } else if (audObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> audList = (List<String>) audObj;
            if (!audList.contains(appleClientId)) {
                throw new JOSEException("Audience mismatch");
            }
        }

        // exp 검증: 토큰 만료 시간 확인
        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if (expirationTime == null || new Date().after(expirationTime)) {
            throw new JOSEException("Token expired");
        }

        return claims;
    }
}
