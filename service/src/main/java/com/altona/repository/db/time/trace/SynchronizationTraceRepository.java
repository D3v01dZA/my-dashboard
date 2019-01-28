package com.altona.repository.db.time.trace;

import com.altona.security.Encryptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;

import static com.altona.util.ObjectMapperHelper.serialize;

@Repository
public class SynchronizationTraceRepository {

    private ObjectMapper objectMapper;
    private SimpleJdbcInsert synchronizationTraceJdbcInsert;

    @Autowired
    public SynchronizationTraceRepository(ObjectMapper objectMapper, DataSource dataSource) {
        this.objectMapper = objectMapper;
        this.synchronizationTraceJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("synchronization_trace")
                .usingGeneratedKeyColumns("id");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(Encryptor encryptor, int userId, int projectId, String stage, Object value) {
        synchronizationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("project_id", projectId)
                .addValue("time", new Date())
                .addValue("stage", stage)
                .addValue("value", encryptor.encrypt(serialize(objectMapper, value))));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(Encryptor encryptor, int userId, int projectId, String stage, String value) {
        synchronizationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("project_id", projectId)
                .addValue("time", new Date())
                .addValue("stage", stage)
                .addValue("value", encryptor.encrypt(value)));
    }

}
