package com.altona.broadcast.service;

import com.altona.broadcast.service.view.UnsavedBroadcastView;
import com.altona.security.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

@AllArgsConstructor
public class UnsavedBroadcast {

    @NonNull
    private String broadcast;

    @NonNull
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Broadcast save(User user) {
        try {
            Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(
                    "INSERT INTO broadcast (broadcast, user_id) VALUES (:broadcast, :userId) RETURNING id",
                    new MapSqlParameterSource()
                            .addValue("broadcast", broadcast)
                            .addValue("userId", user.getId())
            );
            Integer id = (Integer) stringObjectMap.get("id");
            return new Broadcast(id, broadcast, user, jdbcTemplate);
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new IllegalStateException(String.format("Inserting lead to %s rows returned", ex.getActualSize()));
        }
    }

    public UnsavedBroadcastView asView() {
        return new UnsavedBroadcastView(broadcast);
    }

}
