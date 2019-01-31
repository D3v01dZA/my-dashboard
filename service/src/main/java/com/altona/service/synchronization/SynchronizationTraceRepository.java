package com.altona.service.synchronization;

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
    public void trace(SynchronizeRequest request, String stage, Object value) {
        synchronizationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("project_id", request.getProject().getId())
                .addValue("synchronization_id", request.getSynchronizationId())
                .addValue("attempt_id", request.getAttemptId())
                .addValue("time", new Date())
                .addValue("stage", stage)
                .addValue("value", request.encrypt(serialize(objectMapper, value))));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(SynchronizeRequest request, String stage, String value) {
        synchronizationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("project_id", request.getProject().getId())
                .addValue("synchronization_id", request.getSynchronizationId())
                .addValue("attempt_id", request.getAttemptId())
                .addValue("time", new Date())
                .addValue("stage", stage)
                .addValue("value", request.encrypt(value)));
    }

}
