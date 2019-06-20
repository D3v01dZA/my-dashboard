package com.altona.service.broadcast;

import com.altona.security.User;
import com.altona.util.threading.TransactionalThreading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BroadcastService {

    private BroadcastRepository broadcastRepository;
    private FirebaseMessaging firebaseMessaging;
    private TransactionalThreading transactionalThreading;
    private ObjectMapper objectMapper;

    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        broadcastUpdate.getOldBroadcast().ifPresent(broadcastId -> broadcastRepository.delete(user, broadcastId));
        int id = broadcastRepository.insert(user, broadcastUpdate.getNewBroadcast());
        return broadcastRepository.select(user, id).get();
    }

    public List<Broadcast> broadcasts(User user) {
        return broadcastRepository.select(user);
    }

    public void broadcast(User user, BroadcastMessage<?> broadcastMessage) {
        transactionalThreading.executeInReadOnlyTransaction(() -> {
            try {
                log.info("Broadcasting to user {}", user.getId());
                List<String> broadcasts = broadcastRepository.select(user).stream()
                        .map(Broadcast::getBroadcast)
                        .collect(Collectors.toList());

                MulticastMessage message = MulticastMessage.builder()
                        .setAndroidConfig(
                                AndroidConfig.builder()
                                        .putData("type", broadcastMessage.getType().name())
                                        .putData("message", objectMapper.writeValueAsString(broadcastMessage.getMessage()))
                                        .build()
                        )
                        .addAllTokens(broadcasts)
                        .build();

                BatchResponse response = firebaseMessaging.sendMulticast(message);
                log.info("Broadcasted to user {} - {} succeeded and {} failed", user.getId(), response.getSuccessCount(), response.getFailureCount());
            } catch (FirebaseMessagingException | JsonProcessingException e) {
                log.error("Exception while broadcasting to user {}", user.getId(), e);
                throw new RuntimeException(e);
            }
        });
    }

}
