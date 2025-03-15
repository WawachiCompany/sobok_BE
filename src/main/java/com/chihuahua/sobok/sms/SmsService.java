package com.chihuahua.sobok.sms;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SmsCertificationUtil smsCertificationUtil;

    public SmsService(RedisTemplate<String, String> redisTemplate, @Autowired SmsCertificationUtil smsCertificationUtil) {
        this.redisTemplate = redisTemplate;
        this.smsCertificationUtil = smsCertificationUtil;
//        this.webClient = webClientBuilder.baseUrl("https://api.coolsms.co.kr").build();
    }

    // 인증 코드 생성
    public String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000)); // 6자리 숫자
    }

    // SMS 전송 및 Redis 저장
    public void sendSms(String phoneNumber) {

        String verificationCode = generateVerificationCode();

        // Redis에 저장 (Key: phoneNumber, Value: verificationCode, TTL: 3분)
        redisTemplate.opsForValue().set(phoneNumber, verificationCode, 3, TimeUnit.MINUTES);

        // SMS 전송
        smsCertificationUtil.sendSMS(phoneNumber, verificationCode);

    }

    // 인증 코드 검증
    public boolean verifyCode(String phoneNumber, String code) {
        String storedCode = redisTemplate.opsForValue().get(phoneNumber);
        return storedCode != null && storedCode.equals(code);
    }
}
