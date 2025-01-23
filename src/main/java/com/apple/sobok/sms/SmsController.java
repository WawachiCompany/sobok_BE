package com.apple.sobok.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;


    @PostMapping("/send")
    public ResponseEntity<String> sendSms(@Validated @RequestBody SmsRequestDto requestDto) {

        try {
            smsService.sendSms(requestDto.getPhoneNumber());
            return ResponseEntity.ok("인증번호 전송 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("인증번호 전송 실패");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody SmsRequestDto requestDto) {
        boolean success = smsService.verifyCode(requestDto.getPhoneNumber(), requestDto.getCode());
        if (success) {
            return ResponseEntity.ok("유효한 인증 코드");
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 인증 코드");
        }
    }
}
