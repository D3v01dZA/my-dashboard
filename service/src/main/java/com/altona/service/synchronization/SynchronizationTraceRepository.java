package com.altona.service.synchronization;

import com.altona.security.UserContext;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.altona.service.time.util.TimeConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;

import static com.altona.util.ObjectMapperHelper.deserialize;
import static com.altona.util.ObjectMapperHelper.serialize;

@Repository
public class SynchronizationTraceRepository {

    private ObjectMapper objectMapper;
    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert synchronizationTraceJdbcInsert;

    @Autowired
    public SynchronizationTraceRepository(ObjectMapper objectMapper, NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.objectMapper = objectMapper;
        this.namedJdbc = namedJdbc;
        this.synchronizationTraceJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("synchronization_trace")
                .usingGeneratedKeyColumns("id");
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(SynchronizeRequest request, String stage, TakesScreenshot state) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("image", state.getScreenshotAs(OutputType.BASE64));
        trace(request, stage, root);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(SynchronizeRequest request, String stage, Object value) {
        trace(request, stage, serialize(objectMapper, value));
    }

    public List<SynchronizationTrace> traces(UserContext userContext, int projectId, int synchronizationId, String attemptId) {
        return namedJdbc.query("SELECT id, project_id, synchronization_id, attempt_id, time, stage, value" +
                        " FROM synchronization_trace" +
                        " WHERE project_id = :projectId" +
                        " AND synchronization_id = :synchronizationId" +
                        " AND attempt_id = :attemptId",
                new MapSqlParameterSource()
                        .addValue("projectId", projectId)
                        .addValue("synchronizationId", synchronizationId)
                        .addValue("attemptId", attemptId),
                (rs, rn) -> new SynchronizationTrace(
                        rs.getInt("id"),
                        rs.getInt("project_id"),
                        rs.getInt("synchronization_id"),
                        rs.getString("attempt_id"),
                        userContext.localize(new Date(rs.getDate("time").getTime())),
                        rs.getString("stage"),
                        deserialize(objectMapper, userContext.decrypt(rs.getString("value")), JsonNode.class)
                )
        );
    }

}
