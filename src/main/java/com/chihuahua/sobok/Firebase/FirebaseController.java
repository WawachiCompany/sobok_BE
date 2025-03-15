package com.chihuahua.sobok.Firebase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fcm")
public class FirebaseController {

    private final FirebaseService firebaseService;

    @PostMapping("/send")
    public void sendPushNotification(FcmMessageRequestDto fcmMessageRequestDto) {
        firebaseService.sendPushNotification(fcmMessageRequestDto.getTitle(), fcmMessageRequestDto.getBody());
    }

    @PostMapping("/register")
    public void registerFcmToken(String fcmToken) {
        firebaseService.registerFcmToken(fcmToken);
    }
}
