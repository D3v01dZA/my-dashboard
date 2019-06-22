package com.altona.service.broadcast;

import com.altona.security.User;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FirebaseInteractor {

    private FirebaseMessaging firebaseMessaging;

    public FirebaseInteractor(@Value("${firebase.key}") String firebaseKeyLocation) throws IOException {
        FileInputStream file = new FileInputStream(firebaseKeyLocation);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(file))
                .build();
        firebaseMessaging = FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options));
    }

    public void send(User user, List<String> tokens, Map<String, String> data) {
        try {
            MulticastMessage message = MulticastMessage.builder()
                    .setAndroidConfig(
                            AndroidConfig.builder()
                                    .putAllData(data)
                                    .build()
                    )
                    .addAllTokens(tokens)
                    .build();

            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("Broadcasted to user {} - {} succeeded and {} failed", user.getId(), response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Broadcast to user {} failed", user.getId(), e);
        }
    }

}
