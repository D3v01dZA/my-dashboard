package com.altona.broadcast.service;

import com.altona.broadcast.broadcaster.BroadcastToken;
import com.altona.broadcast.service.view.BroadcastView;
import com.altona.security.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@AllArgsConstructor
public class Broadcast {

    private final int id;

    @NonNull
    private final String broadcast;

    @NonNull
    private final User user;

    @NonNull
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UnsavedBroadcast delete() {
        int rows = jdbcTemplate.update(
                "DELETE FROM broadcast WHERE user_id = :userId AND id = :id",
                new MapSqlParameterSource()
                        .addValue("userId", user.getId())
                        .addValue("id", id)
        );
        if (rows != 1) {
            throw new IllegalStateException(String.format("Could not delete broadcast with id %s - %s rows modified", id, rows));
        }
        return new UnsavedBroadcast(broadcast, jdbcTemplate);
    }

    public BroadcastView asView() {
        return new BroadcastView(id, broadcast);
    }

    public BroadcastToken asToken() {
        return new BroadcastToken(broadcast);
    }

}
