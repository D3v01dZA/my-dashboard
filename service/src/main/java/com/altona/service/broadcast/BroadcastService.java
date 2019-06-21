package com.altona.service.broadcast;

import com.altona.security.User;
import com.altona.util.threading.TransactionalThreading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BroadcastService {

    private BroadcastRepository broadcastRepository;
    private FirebaseInteractor firebaseInteractor;
    private TransactionalThreading transactionalThreading;
    private ObjectMapper objectMapper;

    public Broadcast update(User user, BroadcastUpdate broadcastUpdate) {
        broadcastUpdate.getOldBroadcast().ifPresent(broadcast -> delete(user, new BroadcastDelete(broadcast)));
        return broadcastRepository.selectByBroadcast(user, broadcastUpdate.getNewBroadcast())
                .orElseGet(() -> {
                    int id = broadcastRepository.insert(user, broadcastUpdate.getNewBroadcast());
                    return broadcastRepository.select(user, id).get();
                });
    }

    public List<Broadcast> broadcasts(User user) {
        return broadcastRepository.select(user);
    }

    public Optional<Broadcast> delete(User user, BroadcastDelete broadcastDelete) {
        Optional<Broadcast> broadcast = broadcastRepository.selectByBroadcast(user, broadcastDelete.getBroadcast());
        broadcast.ifPresent(value -> broadcastRepository.delete(user, value.getId()));
        return broadcast;
    }

    public Optional<Broadcast> broadcast(User user, int broadcastId) {
        return broadcastRepository.select(user, broadcastId);
    }

    public void broadcast(User user, BroadcastMessage<?> broadcastMessage) {
        transactionalThreading.executeInReadOnlyTransaction(() -> {
            try {
                log.info("Broadcasting to user {}", user.getId());
                List<String> broadcasts = broadcastRepository.select(user).stream()
                        .map(Broadcast::getBroadcast)
                        .collect(Collectors.toList());

                HashMap<String, String> data = Maps.newHashMap();
                data.put("type", broadcastMessage.getType().name());
                data.put("message", objectMapper.writeValueAsString(broadcastMessage.getMessage()));
                firebaseInteractor.send(user, broadcasts, data);
            } catch (JsonProcessingException e) {
                log.error("Exception while serializing", e);
                throw new RuntimeException(e);
            }
        });
    }

}
