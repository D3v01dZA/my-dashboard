package com.altona.project.time.query;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.NoTime;
import com.altona.project.time.RunningBreak;
import com.altona.project.time.RunningWork;
import com.altona.project.time.CurrentTime;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class StartedTimeByUser {

    @NonNull
    private EncryptionContext encryptionContext;

    public CurrentTime execute() {
        List<TimeRow> times = encryptionContext.query(
                "SELECT time.id, time.type, time.start_time, time.end_time, time.project_id, project.name as project_name " +
                        "FROM time " +
                        "INNER JOIN project ON project.id = time.project_id " +
                        "WHERE time.end_time IS NULL AND project.user_id = :userId " +
                        "ORDER BY time.start_time ASC",
                new MapSqlParameterSource()
                        .addValue("userId", encryptionContext.userId()),
                (rs, rn) -> new TimeRow(
                        rs.getInt("id"),
                        TimeRow.Type.valueOf(rs.getString("type")),
                        PostgresTimeReader.padMissingZeroMillis(rs.getString("start_time")),
                        PostgresTimeReader.padMissingZeroMillis(rs.getString("end_time")),
                        rs.getInt("project_id"),
                        rs.getString("project_name")
                )
        );
        if (times.isEmpty()) {
            return new NoTime(encryptionContext);
        }
        if (times.size() == 1) {
            TimeRow workRow = times.get(0);
            Assert.isTrue(workRow.type == TimeRow.Type.WORK, "Expected only un-ended time to be WORK");
            Project project = new Project(encryptionContext, workRow.projectId, workRow.projectName);
            return new RunningWork(encryptionContext, project, workRow.id, workRow.startTime);
        } else if (times.size() == 2) {
            TimeRow workRow = times.get(0);
            Assert.isTrue(workRow.type == TimeRow.Type.WORK, "Expected first un-ended time to be WORK");
            TimeRow breakRow = times.get(1);
            Assert.isTrue(breakRow.type == TimeRow.Type.BREAK, "Expected second un-ended time to be BREAK");
            Assert.isTrue(workRow.projectId == breakRow.projectId, "Expected project id to be the same for WORK and BREAK");
            Project project = new Project(encryptionContext, workRow.projectId, workRow.projectName);
            return new RunningBreak(encryptionContext, project, workRow.id, workRow.startTime, breakRow.id, breakRow.startTime);
        }
        throw new IllegalStateException("Found more than two un-ended times " + times);
    }

    @ToString
    @AllArgsConstructor
    private static class TimeRow {

        private int id;

        @NonNull
        private Type type;

        @NonNull
        private Instant startTime;

        private Instant endTime;

        private int projectId;

        @NonNull
        private String projectName;

        private enum Type {

            WORK,
            BREAK

        }

    }

}
