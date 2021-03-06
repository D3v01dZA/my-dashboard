package com.altona.service.time;

import com.altona.service.project.model.Project;
import com.altona.service.time.model.Time;
import com.altona.service.time.model.TimeType;
import com.altona.service.time.util.TimeInfo;
import com.altona.user.service.User;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class TimeRepository {

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * Exists because dates and date formats are so bad
     * Also this will quite definitely break if the db changes
     * Reason this exists:
     * App Inserts Date        2019-01-26 18:45:08.460-05
     * DB Saves Date           2019-01-26 18:45:08.46-05
     * App Retrieves Date      2019-01-26 18:45:08.046-05
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    private static final int MINIMUM_LENGTH = 22;
    private static final int EXPECTED_LENGTH = 26;

    private static Instant padMissingZeroMillis(String date) {
        try {
            char padding = '0';
            if (date.length() < MINIMUM_LENGTH) {
                throw new IllegalStateException("Date " + date + " is too short");
            } else if (date.length() == MINIMUM_LENGTH) {
                padding = '.';
            }
            while (date.length() != EXPECTED_LENGTH) {
                // Offset by timezone
                int padLoc = (EXPECTED_LENGTH - (EXPECTED_LENGTH - date.length())) - 3;
                String lhs = date.substring(0, padLoc);
                String rhs = date.substring(padLoc);
                date = lhs + padding + rhs;
                padding = '0';
            }
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(date).toInstant();
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rn) -> {
        String endTime = rs.getString("end_time");
        String startTime = rs.getString("start_time");
        return new Time(
                rs.getInt("id"),
                rs.getString("type"),
                padMissingZeroMillis(startTime),
                endTime == null ? null : padMissingZeroMillis(endTime)
        );
    };

    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert timeJdbcInsert;

    public TimeRepository(NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.namedJdbc = namedJdbc;
        this.timeJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("time")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<Time> select(int projectId, int timeId) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE id = :id AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("id", timeId)
                        .addValue("projectId", projectId)
        );
    }

    public Optional<Time> selectWithNullEnd(int projectId, TimeType type) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE end_time IS NULL AND type = :type AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("type", type.name())
                        .addValue("projectId", projectId)
        );
    }

    public List<Time> selectFromDate(int projectId, Instant fromTime) {
        return namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE project_id = :projectId AND start_time > :fromTime",
                new MapSqlParameterSource()
                        .addValue("projectId", projectId)
                        .addValue("fromTime", Date.from(fromTime)),
                TIME_ROW_MAPPER
        );
    }

    public List<Time> selectBetween(int projectId, Instant from, Instant to) {
        return namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE " +
                        "(" +
                        "   (end_time IS NULL AND ((start_time >= :fromDate AND start_time <= :toDate) OR start_time <= :toDate))" +
                        "   OR (end_time <= :toDate AND start_time >= :fromDate) " +
                        "   OR (start_time <= :fromDate AND end_time >= :fromDate) " +
                        "   OR (end_time <= :toDate AND end_time >= :toDate) " +
                        ") " +
                        "AND project_id = :projectId " +
                        "ORDER BY start_time ASC, end_time ASC",
                new MapSqlParameterSource()
                        .addValue("fromDate", Date.from(from))
                        .addValue("toDate", Date.from(to))
                        .addValue("projectId", projectId),
                TIME_ROW_MAPPER
        );
    }

    public int startTime(int projectId, TimeType type, TimeInfo timeInfo) {
        return timeJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("type", type.name())
                .addValue("start_time", Date.from(timeInfo.now()))
                .addValue("project_id", projectId))
                .intValue();
    }

    public void stopTime(int projectId, int id, TimeInfo timeInfo) {
        namedJdbc.update(
                "UPDATE time SET end_time = :endTime WHERE id = :id AND project_id = :project_id",
                new MapSqlParameterSource()
                        .addValue("endTime", Date.from(timeInfo.now()))
                        .addValue("id", id)
                        .addValue("project_id", projectId)
        );
    }

    public void updateTime(int projectId, int timeId, Instant start, Instant end) {
        namedJdbc.update(
                "UPDATE time SET start_time = :start_time, end_time = :end_time WHERE id = :id AND project_id = :project_id",
                new MapSqlParameterSource()
                        .addValue("start_time", Date.from(start))
                        .addValue("end_time", Date.from(end))
                        .addValue("id", timeId)
                        .addValue("project_id", projectId)
        );
    }

    public void deleteTime(int projectId, int timeId) {
        namedJdbc.update(
                "DELETE FROM time WHERE id = :id AND project_id = :project_id",
                new MapSqlParameterSource()
                        .addValue("id", timeId)
                        .addValue("project_id", projectId)
        );
    }

    private Optional<Time> getSingleTimeFromQuery(String query, MapSqlParameterSource parameters) {
        try {
            return Optional.of(namedJdbc.queryForObject(query, parameters, TIME_ROW_MAPPER));
        } catch (IncorrectResultSizeDataAccessException ex) {
            if (ex.getActualSize() == 0) {
                return Optional.empty();
            } else {
                throw new IllegalStateException("Multiple records found");
            }
        }
    }

}
