package com.altona.service.broadcast;

import com.altona.security.User;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
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

    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        broadcastUpdate.getOldBroadcast().ifPresent(broadcastId -> broadcastRepository.delete(user, broadcastId));
        int id = broadcastRepository.insert(user, broadcastUpdate.getNewBroadcast());
        return broadcastRepository.select(user, id).get();
    }

    public List<Broadcast> broadcasts(User user) {
        return broadcastRepository.select(user);
    }

    public void broadcast(User user) {
        log.info("Broadcasting to user {}", user.getId());
        List<String> broadcasts = broadcastRepository.select(user).stream()
                .map(Broadcast::getBroadcast)
                .collect(Collectors.toList());

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(new Notification("Test", "Message"))
                .addAllTokens(broadcasts)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendMulticast(message);
            log.info("Sent messages, {} succeeded and {} failed", response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
