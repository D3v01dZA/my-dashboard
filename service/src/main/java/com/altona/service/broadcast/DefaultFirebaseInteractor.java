package com.altona.service.broadcast;

import com.altona.security.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Maps;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Profile("!test")
public class DefaultFirebaseInteractor implements FirebaseInteractor {

    private FirebaseMessaging firebaseMessaging;
    private ObjectMapper objectMapper;

    public DefaultFirebaseInteractor(@Value("${firebase.key}") String firebaseKeyLocation, ObjectMapper objectMapper) throws IOException {
        FileInputStream file = new FileInputStream(firebaseKeyLocation);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(file))
                .build();
        this.firebaseMessaging = FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options));
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(User user, List<String> tokens, BroadcastMessage<?> broadcastMessage) {
        try {
            Map<String, String> data = Maps.newHashMap();
            data.put("type", broadcastMessage.getType().name());
            data.put("message", objectMapper.writeValueAsString(broadcastMessage.getMessage()));
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
        } catch (FirebaseMessagingException | JsonProcessingException e) {
            log.error("Broadcast to user {} failed", user.getId(), e);
        }
    }

}
