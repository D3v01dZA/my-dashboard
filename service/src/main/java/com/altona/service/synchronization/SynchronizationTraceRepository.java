package com.altona.service.synchronization;

import com.altona.security.Encryptor;
import com.altona.service.synchronization.model.SynchronizationAttempt;
import com.altona.service.synchronization.model.SynchronizationRequest;
import com.altona.service.synchronization.model.SynchronizationTrace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

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
    public void trace(SynchronizationAttempt attempt, SynchronizationRequest request, String stage, Screenshotter state) {
        trace(request, new SynchronizationTrace(-1, attempt.getId(), stage, Screenshot.take(state)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void trace(Encryptor encryptor, SynchronizationTrace synchronizationTrace) {
        synchronizationTraceJdbcInsert.execute(new MapSqlParameterSource()
                .addValue("synchronization_attempt_id", synchronizationTrace.getSynchronizationAttemptId())
                .addValue("stage", synchronizationTrace.getStage())
                .addValue("screenshot", encryptor.encrypt(synchronizationTrace.getScreenshot().getBase64())));
    }

    public List<SynchronizationTrace> traces(Encryptor encryptor, SynchronizationAttempt synchronizationAttempt) {
        return namedJdbc.query("SELECT id, synchronization_attempt_id, stage, screenshot" +
                        " FROM synchronization_trace" +
                        " WHERE synchronization_attempt_id = :synchronizationAttemptId",
                new MapSqlParameterSource()
                        .addValue("synchronizationAttemptId", synchronizationAttempt.getId()),
                (rs, rn) -> new SynchronizationTrace(
                        rs.getInt("id"),
                        rs.getInt("synchronization_attempt_id"),
                        rs.getString("stage"),
                        Optional.ofNullable(encryptor.decrypt(rs.getString("screenshot"))).map(Screenshot::new).orElse(null)
                )
        );
    }

}
