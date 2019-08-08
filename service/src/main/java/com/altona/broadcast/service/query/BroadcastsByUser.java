package com.altona.broadcast.service.query;

import com.altona.broadcast.service.Broadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

@AllArgsConstructor
public class BroadcastsByUser {

    @NonNull
    private Context context;

    public List<Broadcast> execute() {
        return context.query(
                "SELECT id, broadcast FROM broadcast WHERE user_id = :userId",
                new MapSqlParameterSource()
                        .addValue("userId", context.getUserId()),
                new BroadcastRowMapper(context)
        );
    }

}
