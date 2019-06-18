package com.altona.service.synchronization;

import com.altona.security.Encryptor;
import com.altona.service.synchronization.model.Synchronization;
import com.altona.util.ObjectMapperHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Repository
public class SynchronizationRepository {

    private ObjectMapper objectMapper;
    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert synchronizationJdbcInsert;

    public SynchronizationRepository(ObjectMapper objectMapper, NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.objectMapper = objectMapper;
        this.namedJdbc = namedJdbc;
        this.synchronizationJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("synchronization")
                .usingGeneratedKeyColumns("id");
    }

    public int insert(Encryptor encryptor, int projectId, Synchronization synchronization) {
        return synchronizationJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("service", synchronization.getService().name())
                .addValue("configuration",
                        encryptor.encrypt(
                                ObjectMapperHelper.serialize(objectMapper, synchronization.getConfiguration())
                        )
                )
                .addValue("projectId", projectId)
        ).intValue();
    }

    public void update(Encryptor encryptor, int projectId, Synchronization synchronization) {
        int updated = namedJdbc.update(
                "UPDATE synchronization SET service = :service, configuration = :configuration WHERE project_id = :projectId AND id = :synchronizationId",
                new MapSqlParameterSource()
                        .addValue("service", synchronization.getService().name())
                        .addValue("configuration",
                                encryptor.encrypt(
                                        ObjectMapperHelper.serialize(objectMapper, synchronization.getConfiguration())
                                ))
                        .addValue("projectId", projectId)
                        .addValue("synchronizationId", synchronization.getId())
        );
        if (updated != 1) {
            throw new RuntimeException("Didn't update the correct number of rows, should have been 1, was " + updated);
        }
    }

    public List<Synchronization> select(Encryptor encryptor, int projectId) {
        return namedJdbc.query(
                "SELECT id, service, configuration FROM synchronization WHERE project_id = :projectId",
                new MapSqlParameterSource("projectId", projectId),
                rowMapper(encryptor, objectMapper)
        );
    }

    public Optional<Synchronization> select(Encryptor encryptor, int projectId, int id) {
        try {
            return Optional.of(namedJdbc.queryForObject(
                    "SELECT id, service, configuration FROM synchronization WHERE project_id = :projectId and id = :id",
                    new MapSqlParameterSource()
                            .addValue("projectId", projectId)
                            .addValue("id", id),
                    rowMapper(encryptor, objectMapper)
            ));
        } catch (IncorrectResultSizeDataAccessException ex) {
            if (ex.getActualSize() == 0) {
                return Optional.empty();
            } else {
                throw new IllegalStateException("Multiple records found");
            }
        }
    }

    private static RowMapper<Synchronization> rowMapper(Encryptor encryptor, ObjectMapper objectMapper) {
        return (rs, rn) -> {
            try {
                return new Synchronization(
                        rs.getInt("id"),
                        rs.getString("service"),
                        objectMapper.readValue(
                                encryptor.decrypt(rs.getString("configuration")),
                                ObjectNode.class
                        )
                );
            } catch (IOException e) {
                // Password changes right?
                throw new IllegalStateException(e);
            }
        };
    }

}
