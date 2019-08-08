package com.altona.broadcast.service.operation;

import com.altona.broadcast.service.Broadcast;
import com.altona.broadcast.service.Broadcasts;
import com.altona.broadcast.service.UnsavedBroadcast;
import com.altona.security.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@AllArgsConstructor
public class BroadcastUpdate {

    private String oldBroadcast;

    @NonNull
    private String newBroadcast;

    public Broadcast execute(Broadcasts broadcasts, NamedParameterJdbcTemplate jdbcTemplate, User user) {
        if (oldBroadcast != null) {
            broadcasts.broadcastByBroadcast(user, oldBroadcast)
                    .ifPresent(Broadcast::delete);
        }
        return broadcasts.broadcastByBroadcast(user, newBroadcast)
                .orElseGet(() -> new UnsavedBroadcast(newBroadcast, jdbcTemplate).save(user));
    }

}
