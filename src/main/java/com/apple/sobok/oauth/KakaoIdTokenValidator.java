package com.apple.sobok.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Base64;

@Service
public class KakaoIdTokenValidator {

    private final KakaoPublicKeyService kakaoPublicKeyService;

    public KakaoIdTokenValidator(KakaoPublicKeyService kakaoPublicKeyService) {
        this.kakaoPublicKeyService = kakaoPublicKeyService;
    }

    public Claims validateIdToken(String idToken) {
        try {
            String[] tokenParts = idToken.split("\\.");
            String header = new String(Base64.getDecoder().decode(tokenParts[0]));
            JsonNode headerNode = new ObjectMapper().readTree(header);
            JsonNode kidNode = headerNode.get("kid");

            if (kidNode == null) {
                throw new IllegalArgumentException("Invalid ID token: 'kid' not found in header");
            }

            String kid = kidNode.asText();

            PublicKey publicKey = kakaoPublicKeyService.getPublicKey(kid);

            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken).getPayload();
        } catch (SignatureException e) {
            throw new IllegalArgumentException("Invalid ID token signature", e);
        } catch (Exception e) {
            throw new RuntimeException("ID token validation failed", e);
        }
    }
}
