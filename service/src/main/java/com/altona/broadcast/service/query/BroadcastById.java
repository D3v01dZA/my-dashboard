package com.altona.broadcast.service.query;

import com.altona.broadcast.service.Broadcast;
import com.altona.context.Context;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Optional;

@AllArgsConstructor
public class BroadcastById {

    @NonNull
    private Context context;

    private int id;

    public Optional<Broadcast> execute() {
        try {
            return Optional.of(
                    context.queryForObject(
                            "SELECT id, broadcast FROM broadcast WHERE user_id = :userId AND id = :id",
                            new MapSqlParameterSource()
                                    .addValue("id", id)
                                    .addValue("userId", context.getUserId()),
                            new BroadcastRowMapper(context)
                    )
            );
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

}
