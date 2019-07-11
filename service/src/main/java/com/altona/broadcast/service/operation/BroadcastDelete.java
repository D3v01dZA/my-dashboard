package com.altona.broadcast.service.operation;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.Broadcasts;
import com.altona.broadcast.service.UnsavedBroadcast;
import com.altona.security.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@AllArgsConstructor
public class BroadcastDelete {

    @NonNull
    private String broadcast;

    public Optional<UnsavedBroadcast> execute(Broadcasts broadcasts, User user) {
        return broadcasts.broadcastByBroadcast(user, broadcast)
                .map(Broadcast::delete);
    }

}
