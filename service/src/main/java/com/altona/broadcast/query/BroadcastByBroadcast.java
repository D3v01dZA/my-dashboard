package com.altona.broadcast.query;

import com.altona.broadcast.Broadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Optional;

@AllArgsConstructor
public class BroadcastByBroadcast {

    @NonNull
    private Context context;

    @NonNull
    private String broadcast;

    public Optional<Broadcast> execute() {
        try {
            return Optional.of(
                    context.queryForObject(
                            "SELECT id, broadcast FROM broadcast WHERE user_id = :userId AND broadcast = :broadcast",
                            new MapSqlParameterSource()
                                    .addValue("broadcast", broadcast)
                                    .addValue("userId", context.userId()),
                            new BroadcastRowMapper(context)
                    )
            );
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

}
