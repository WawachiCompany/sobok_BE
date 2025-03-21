package com.chihuahua.sobok.oauth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class AppleClientSecretGenerator {
    public static String generateClientSecret(String teamId, String clientId, String keyId, String privateKeyContent) throws Exception {
        // PEM 헤더와 푸터, 그리고 공백/줄바꿈 제거
        String privateKeyPem = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(86400 * 180); // 예: 6개월

        return Jwts.builder()
                .header().add("kid", keyId).and()
                .issuer(teamId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .audience().add("https://appleid.apple.com").and()
                .subject(clientId)
                .signWith(privateKey)
                .compact();
    }
}
