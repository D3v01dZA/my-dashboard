package com.altona.project.time.modify;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.TimeType;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.Date;
import java.util.Map;

@AllArgsConstructor
public class StartTime {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project project;

    @NonNull
    private TimeType timeType;

    public int execute() {
        Map<String, Object> result = encryptionContext.queryForMap(
                "INSERT INTO time (type, start_time, project_id) VALUES (:type, :startTime, :projectId) RETURNING id",
                new MapSqlParameterSource()
                        .addValue("startTime", Date.from(encryptionContext.now()))
                        .addValue("type", timeType)
                        .addValue("projectId", project.id())
        );
        return (int) result.get("id");
    }

}
