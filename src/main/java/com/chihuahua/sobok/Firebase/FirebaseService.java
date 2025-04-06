package com.chihuahua.sobok.Firebase;

import com.chihuahua.sobok.member.Member;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FirebaseService {

    private final FcmTokenRepository fcmTokenRepository;

    public void sendPushNotification(String title, String body, Member member) {

        Optional<FcmToken> fcmToken = fcmTokenRepository.findByMemberId(member.getId());
        fcmToken.ifPresent(token -> sendMessage(token.getFcmToken(), title, body));
    }

    public void sendMessage(String token, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("푸시 메시지 전송 성공: " + response);
        } catch (FirebaseMessagingException e) {
            System.out.println("푸시 메시지 전송 실패: " + e.getMessage());
        }
    }

    public void registerFcmToken(String token, Member member) {

        Optional<FcmToken> existingFcmToken = fcmTokenRepository.findByMemberIdAndFcmToken(member.getId(), token);
        FcmToken fcmToken;
        if(existingFcmToken.isPresent()) {
            fcmToken = existingFcmToken.get();
            fcmToken.setUpdatedAt(LocalDateTime.now());
            fcmToken.setActive(true);
        }
        else {
            fcmToken = new FcmToken();
            fcmToken.setMemberId(member.getId());
            fcmToken.setFcmToken(token);
            fcmToken.setActive(true);
            fcmToken.setCreatedAt(LocalDateTime.now());
            fcmToken.setUpdatedAt(LocalDateTime.now());
        }
        fcmTokenRepository.save(fcmToken);
    }
}
