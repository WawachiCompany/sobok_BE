package com.chihuahua.sobok.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class KakaoPublicKeyService {

    private static final String KAKAO_JWKS_URL = "https://kauth.kakao.com/.well-known/jwks.json";

    public PublicKey getPublicKey(String kid) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String jwksResponse = restTemplate.getForObject(KAKAO_JWKS_URL, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jwks = objectMapper.readTree(jwksResponse).get("keys");

        for (JsonNode key : jwks) {
            if (key.get("kid").asText().equals(kid)) {
                String n = key.get("n").asText();
                String e = key.get("e").asText();

                byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
                byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

                BigInteger modulus = new BigInteger(1, modulusBytes);
                BigInteger exponent = new BigInteger(1, exponentBytes);

                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return keyFactory.generatePublic(spec);
            }
        }
        throw new IllegalArgumentException("Public key with kid not found");
    }
}
