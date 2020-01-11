package com.altona.project.time.query;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import com.altona.project.time.TimeType;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.altona.project.time.query.PostgresTimeReader.padMissingZeroMillis;

@AllArgsConstructor
class TimeEntriesBetween {

    @NonNull
    private EncryptionContext encryptionContext;

    @NonNull
    private Project project;

    @NonNull
    private LocalDate from;

    @NonNull
    private LocalDate to;

    public List<TimeEntry> execute() {
        // Query
        List<TimeRecord> timeRecords = encryptionContext.query(
                "SELECT id, type, start_time, end_time FROM time WHERE " +
                        "(" +
                        "   (end_time IS NULL AND ((start_time > :fromDate AND start_time < :toDate) OR start_time < :toDate))" +
                        "   OR (end_time < :toDate AND start_time > :fromDate) " +
                        "   OR (start_time < :fromDate AND end_time > :fromDate) " +
                        "   OR (end_time < :toDate AND end_time > :toDate) " +
                        ") " +
                        "AND project_id = :projectId " +
                        "ORDER BY start_time ASC, end_time ASC",
                new MapSqlParameterSource()
                        .addValue("fromDate", Date.from(encryptionContext.unlocalize(from)))
                        .addValue("toDate", Date.from(encryptionContext.unlocalize(to)))
                        .addValue("projectId", project.id()),
                (rs, rn) -> new TimeRecord(
                        rs.getInt("id"),
                        TimeType.valueOf(rs.getString("type")),
                        padMissingZeroMillis(rs.getString("start_time")),
                        padMissingZeroMillis(rs.getString("end_time"))
                )
        );

        // Arrange into time combinations
        List<TimeEntry> timeCombinations = new ArrayList<>();
        TimeRecord workRow = null;
        List<TimeRecord> breakRows = null;
        for (TimeRecord timeRecord : timeRecords) {
            if (workRow == null) {
                Assert.isTrue(timeRecord.isWork(), () -> String.format("First work %s was supposed to be a break", timeRecord));
                workRow = timeRecord;
                breakRows = new ArrayList<>();
            } else {
                if (timeRecord.isWork()) {
                    timeCombinations.add(new TimeEntry(workRow, breakRows));
                    workRow = null;
                    breakRows = null;
                } else {
                    breakRows.add(timeRecord);
                }
            }
        }
        return timeCombinations;
    }

}
