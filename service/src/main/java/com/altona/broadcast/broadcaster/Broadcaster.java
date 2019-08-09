package com.altona.broadcast.broadcaster;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.query.BroadcastsByUser;
import com.altona.context.Context;
import com.altona.context.SqlContext;
import com.altona.user.service.User;
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

    private BroadcastInteractor broadcastInteractor;
    private TransactionalThreading transactionalThreading;
    private SqlContext sqlContext;

    public void broadcast(User user, BroadcastMessage<?> broadcastMessage) {
        broadcast(Context.of(sqlContext, user), broadcastMessage);
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
