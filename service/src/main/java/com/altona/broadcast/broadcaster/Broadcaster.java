package com.altona.broadcast.broadcaster;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.Broadcasts;
import com.altona.security.User;
import com.altona.util.threading.TransactionalThreading;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class Broadcaster {

    private Broadcasts broadcasts;
    private BroadcastInteractor broadcastInteractor;
    private TransactionalThreading transactionalThreading;

    public void broadcast(User user, BroadcastMessage<?> broadcastMessage) {
        transactionalThreading.executeInReadOnlyTransaction(() -> {
            log.info("Broadcasting to user {}", user.getId());
            List<BroadcastToken> tokens = broadcasts.broadcasts(user).stream()
                    .map(Broadcast::asToken)
                    .collect(Collectors.toList());

            broadcastInteractor.send(user, tokens, broadcastMessage);
        });
    }

}
