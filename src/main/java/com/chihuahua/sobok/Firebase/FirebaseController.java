package com.chihuahua.sobok.Firebase;

import com.chihuahua.sobok.member.Member;
import com.chihuahua.sobok.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FirebaseController {

    private final FirebaseService firebaseService;
    private final MemberService memberService;

    @PostMapping("/send")
    public ResponseEntity<?> sendPushNotification(@RequestBody FcmMessageRequestDto fcmMessageRequestDto) {
        Member member = memberService.getMember();
        try {
            firebaseService.sendPushNotification(fcmMessageRequestDto.getTitle(), fcmMessageRequestDto.getBody(), member);
            return ResponseEntity.ok("푸시 알림 전송 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("푸시 알림 전송 실패: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerFcmToken(@RequestParam String fcmToken) {
        Member member = memberService.getMember();
        try {
            firebaseService.registerFcmToken(fcmToken, member);
            return ResponseEntity.ok("토큰 등록 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("토큰 등록 실패: " + e.getMessage());
        }
    }
}
