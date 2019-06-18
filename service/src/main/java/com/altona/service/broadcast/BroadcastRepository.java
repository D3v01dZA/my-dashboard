package com.altona.service.broadcast;

import com.altona.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class BroadcastRepository {

    private static final RowMapper<Broadcast> BROADCAST_ROW_MAPPER = (rs, rn) -> new Broadcast(rs.getInt("id"), rs.getString("broadcast"));

    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert broadcastJdbcInsert;

    @Autowired
    public BroadcastRepository(NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.namedJdbc = namedJdbc;
        this.broadcastJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("broadcast")
                .usingGeneratedKeyColumns("id");
    }

    public void delete(User user, String broadcast) {
        namedJdbc.update(
                "DELETE FROM broadcast WHERE user_id = :user_id AND broadcast = :broadcast",
                new MapSqlParameterSource()
                        .addValue("user_id", user.getId())
                        .addValue("broadcast", broadcast)
        );
    }

    public int insert(User user, String broadcast) {
        return broadcastJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("user_id", user.getId())
                .addValue("broadcast", broadcast))
                .intValue();
    }

    public Optional<Broadcast> select(User user, int id) {
        try {
            return Optional.of(namedJdbc.queryForObject(
                    "SELECT id, broadcast FROM broadcast WHERE user_id = :userId AND id = :broadcastId",
                    new MapSqlParameterSource()
                            .addValue("broadcastId", id)
                            .addValue("userId", user.getId()),
                    BROADCAST_ROW_MAPPER
            ));
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Broadcast> select(User user) {
        return namedJdbc.query(
                "SELECT id, broadcast FROM broadcast WHERE user_id = :userId",
                new MapSqlParameterSource()
                        .addValue("userId", user.getId()),
                BROADCAST_ROW_MAPPER
        );
    }

}
