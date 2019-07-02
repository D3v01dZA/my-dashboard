package com.altona.service.synchronization;

import com.altona.security.Encryptor;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Optional;

@Repository
public class SynchronizationAttemptRepository {

    private NamedParameterJdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert synchronizationAttemptJdbcInsert;

    @Autowired
    public SynchronizationAttemptRepository(NamedParameterJdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.synchronizationAttemptJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("synchronization_attempt")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<SynchronizationAttempt> select(Encryptor encryptor, Synchronization synchronization, int id) {
        try {
            return Optional.of(jdbcTemplate.queryForObject(
                    "SELECT id, synchronization_id, status, message, screenshot FROM synchronization_attempt WHERE id = :id AND synchronization_id = :synchronizationId",
                    new MapSqlParameterSource()
                            .addValue("id", id)
                            .addValue("synchronizationId", synchronization.getId()),
                    (rs, rn) -> new SynchronizationAttempt(
                            rs.getInt("id"),
                            SynchronizationStatus.valueOf(rs.getString("status")),
                            rs.getString("message"),
                            Optional.ofNullable(rs.getString("screenshot")).map(encryptor::decrypt).map(Screenshot::new).orElse(null),
                            rs.getInt("synchronization_id")
                    )
            ));
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

    public void update(Encryptor encryptor, SynchronizationAttempt synchronizationAttempt) {
        int updated = jdbcTemplate.update(
                "UPDATE synchronization_attempt SET synchronization_id = :synchronizationId, status = :status, message = :message, screenshot = :screenshot WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("id", synchronizationAttempt.getId())
                        .addValue("status", synchronizationAttempt.getStatus().name())
                        .addValue("message", synchronizationAttempt.getMessage().orElse(null))
                        .addValue("screenshot", synchronizationAttempt.getScreenshot()
                                .map(Screenshot::getBase64)
                                .map(encryptor::encrypt)
                                .orElse(null))
                        .addValue("synchronizationId", synchronizationAttempt.getSynchronizationId())
        );
        if (updated != 1) {
            throw new RuntimeException("Didn't update the correct number of rows, should have been 1, was " + updated);
        }
    }

    public SynchronizationAttempt insert(Encryptor encryptor, Synchronization synchronization, SynchronizationAttempt synchronizationAttempt) {
        int id = synchronizationAttemptJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("status", synchronizationAttempt.getStatus().name())
                .addValue("message", synchronizationAttempt.getMessage().orElse(null))
                .addValue("screenshot", synchronizationAttempt.getScreenshot()
                        .map(Screenshot::getBase64)
                        .map(encryptor::encrypt)
                        .orElse(null))
                .addValue("synchronization_id", synchronizationAttempt.getSynchronizationId()))
                .intValue();
        return select(encryptor, synchronization, id).get();
    }

}
