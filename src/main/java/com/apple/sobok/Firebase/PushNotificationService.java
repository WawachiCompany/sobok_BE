//package com.apple.sobok.Firebase;
//
//import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.messaging.Message;
//import com.google.firebase.messaging.Notification;
//import com.google.firebase.messaging.FirebaseMessagingException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class PushNotificationService {
//
//    public void sendPushNotification(String token, String title, String body) {
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//
//        Message message = Message.builder()
//                .setToken(token)
//                .setNotification(notification)
//                .build();
//
//        try {
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("푸시 메시지 전송 성공: " + response);
//        } catch (FirebaseMessagingException e) {
//            e.printStackTrace();
//        }
//    }
//}
