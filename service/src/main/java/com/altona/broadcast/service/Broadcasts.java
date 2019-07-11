package com.altona.broadcast.service;

import com.altona.security.User;
import lombok.AllArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class Broadcasts {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Broadcast save(User user, UnsavedBroadcast unsavedBroadcast) {
        return unsavedBroadcast.save(user, jdbcTemplate);
    }

    public Optional<Broadcast> broadcast(User user, int id) {
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject(
                            "SELECT id, broadcast FROM broadcast WHERE user_id = :userId AND id = :id",
                            new MapSqlParameterSource()
                                    .addValue("id", id)
                                    .addValue("userId", user.getId()),
                            rowMapper(user)
                    )
            );
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<Broadcast> broadcastByBroadcast(User user, String broadcast) {
        try {
            return Optional.of(
                    jdbcTemplate.queryForObject(
                            "SELECT id, broadcast FROM broadcast WHERE user_id = :userId AND broadcast = :broadcast",
                            new MapSqlParameterSource()
                                    .addValue("broadcast", broadcast)
                                    .addValue("userId", user.getId()),
                            rowMapper(user)
                    )
            );
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Broadcast> broadcasts(User user) {
        return jdbcTemplate.query(
                "SELECT id, broadcast FROM broadcast WHERE user_id = :userId",
                new MapSqlParameterSource()
                        .addValue("userId", user.getId()),
                rowMapper(user)
        );
    }

    private RowMapper<Broadcast> rowMapper(User user) {
        return (rs, rn) -> new Broadcast(rs.getInt("id"), rs.getString("broadcast"), user, jdbcTemplate);
    }


}
