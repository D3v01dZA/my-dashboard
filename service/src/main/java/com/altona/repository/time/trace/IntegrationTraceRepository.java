package com.altona.repository.time.trace;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.altona.util.ObjectMapperHelper.serialize;

@Repository
public class IntegrationTraceRepository {

    private ObjectMapper objectMapper;
    private SimpleJdbcInsert integrationTraceJdbcInsert;

    @Autowired
    public IntegrationTraceRepository(ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.objectMapper = objectMapper;
        this.integrationTraceJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("integration_trace")
                .usingGeneratedKeyColumns("id");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(int userId, int projectId, String stage, Object value) {
        integrationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("project_id", projectId)
                .addValue("time", new Date())
                .addValue("stage", stage)
                .addValue("value", serialize(objectMapper, value)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(int userId, int projectId, String stage, String value) {
        integrationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("project_id", projectId)
                .addValue("time", new Date())
                .addValue("stage", stage)
                .addValue("value", value));
    }

}
