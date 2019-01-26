package com.altona.db.time.time;

import org.postgresql.util.PGTimestamp;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class TimeRepository {

    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rn) -> {
        try {
            String endTime = rs.getString("end_time");
            String startTime = rs.getString("start_time");
            return new Time(
                    rs.getInt("id"),
                    rs.getString("type"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(startTime),
                    endTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(endTime)
            );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    };

    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert timeJdbcInsert;

    public TimeRepository(NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.namedJdbc = namedJdbc;
        this.timeJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("time")
                .usingGeneratedKeyColumns("id");
    }

    public List<Time> timeList(int projectId) {
        return namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE project_id = :projectId",
                new MapSqlParameterSource("projectId", projectId),
                TIME_ROW_MAPPER
        );
    }

    public Optional<Time> time(int projectId, int timeId) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE id = :id AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("id", timeId)
                        .addValue("projectId", projectId)
        );
    }

    public Optional<Time> timeWithNullEnd(int projectId, Time.Type type) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE end_time IS NULL AND type = :type AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("type", type.name())
                        .addValue("projectId", projectId)
        );
    }

    public List<Time> timesFromDate(int projectId, Date fromTime) {
        return namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE project_id = :projectId AND start_time > :fromTime",
                new MapSqlParameterSource()
                        .addValue("projectId", projectId)
                        .addValue("fromTime", new java.sql.Timestamp(fromTime.getTime())),
                TIME_ROW_MAPPER
        );
    }

    public List<Time> timeListBetween(int projectId, Date from, Date to) {
        return namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE end_time IS NOT NULL " +
                        "AND (end_time < :toDate AND start_time > :fromDate) " +
                        "OR (end_time > :toDate AND start_time > :fromDate) " +
                        "OR (end_time < :toDate AND start_time < :fromDate) " +
                        "OR (end_time > :toDate AND start_time < :fromDate) " +
                        "AND project_id = :projectId " +
                        "ORDER BY start_time DESC, end_time DESC",
                new MapSqlParameterSource()
                        .addValue("fromDate", new java.sql.Timestamp(from.getTime()))
                        .addValue("toDate", new java.sql.Timestamp(to.getTime()))
                        .addValue("projectId", projectId),
                TIME_ROW_MAPPER
        );
    }

    public int startTime(int projectId, Time.Type type) {
        return timeJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("type", type.name())
                .addValue("start_time", new java.sql.Timestamp(new Date().getTime()))
                .addValue("project_id", projectId))
                .intValue();
    }

    public void stopTime(int id, Date stopTime) {
        namedJdbc.update(
                "UPDATE time SET end_time = :endTime WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("endTime", new java.sql.Timestamp(stopTime.getTime()))
                        .addValue("id", id)
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
