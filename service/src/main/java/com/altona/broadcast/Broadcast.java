package com.altona.broadcast;

import com.altona.broadcast.broadcaster.BroadcastToken;
import com.altona.broadcast.view.BroadcastView;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@AllArgsConstructor
public class Broadcast {

    @NonNull
    private Context context;

    private int id;

    @NonNull
    private String broadcast;

    public UnsavedBroadcast delete() {
        int rows = context.update(
                "DELETE FROM broadcast WHERE user_id = :userId AND id = :id",
                new MapSqlParameterSource()
                        .addValue("userId", context.userId())
                        .addValue("id", id)
        );
        if (rows != 1) {
            throw new IllegalStateException(String.format("Could not delete broadcast with id %s - %s rows modified", id, rows));
        }
        return new UnsavedBroadcast(context, broadcast);
    }

    public BroadcastView asView() {
        return new BroadcastView(id, broadcast);
    }

    public BroadcastToken asToken() {
        return new BroadcastToken(broadcast);
    }

}
