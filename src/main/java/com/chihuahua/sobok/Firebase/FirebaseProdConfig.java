package com.chihuahua.sobok.Firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("prod")
public class FirebaseProdConfig {

    @Value("${FIREBASE_ADMIN_SDK}")
    private String firebaseAdminSdk;

    @PostConstruct
    public void initialize() {
        // 환경 변수에서 읽어온 JSON 문자열에서 "\n"을 실제 개행 문자로 변환
        String formattedJson = firebaseAdminSdk.replace("\\n", "\n");
        InputStream serviceAccountStream = new ByteArrayInputStream(formattedJson.getBytes(StandardCharsets.UTF_8));

        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException("Firebase 초기화에 실패했습니다.", e);
        }
    }
}