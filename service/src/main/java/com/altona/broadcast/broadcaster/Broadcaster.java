package com.altona.broadcast.broadcaster;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.query.BroadcastsByUser;
import com.altona.context.Context;
import com.altona.security.User;
import com.altona.util.threading.TransactionalThreading;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class Broadcaster {

    private BroadcastInteractor broadcastInteractor;
    private TransactionalThreading transactionalThreading;
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void broadcast(User user, BroadcastMessage<?> broadcastMessage) {
        broadcast(Context.of(user, jdbcTemplate), broadcastMessage);
    }

    public void broadcast(Context context, BroadcastMessage<?> broadcastMessage) {
        transactionalThreading.executeInReadOnlyTransaction(() -> {
            log.info("Broadcasting to user {}", context.getUserId());
            List<BroadcastToken> tokens = new BroadcastsByUser(context).execute().stream()
                    .map(Broadcast::asToken)
                    .collect(Collectors.toList());

            broadcastInteractor.send(context, tokens, broadcastMessage);
        });
    }

}
