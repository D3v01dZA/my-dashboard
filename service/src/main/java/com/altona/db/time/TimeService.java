package com.altona.db.time;

import com.altona.db.time.control.BreakStart;
import com.altona.db.time.control.BreakStop;
import com.altona.db.time.control.WorkStart;
import com.altona.db.time.control.WorkStop;
import com.altona.db.time.summary.Summary;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.altona.db.time.control.TimeServiceRunningHelpers.runningBreakAwareFunction;
import static com.altona.db.time.control.TimeServiceRunningHelpers.runningWorkAwareFunction;

@Service
public class TimeService {

    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rn) -> {
        try {
            String endTime = rs.getString("end_time");
            String startTime = rs.getString("start_time");
            return new Time(
                    rs.getInt("id"),
                    rs.getString("type"),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(startTime),
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX").parse(endTime)
            );
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    };

    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert timeJdbcInsert;

    public TimeService(NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.namedJdbc = namedJdbc;
        this.timeJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("time")
                .usingGeneratedKeyColumns("id");
    }

    public WorkStart startProjectWork(Project project) {
        return runningWorkAwareFunction(this, project,
                runningWork -> WorkStart.alreadyStarted(runningWork.getId()),
                () -> WorkStart.started(startTime(project, Time.Type.WORK).getId())
        );
    }

    public WorkStop endProjectWork(Project project) {
        return runningWorkAwareFunction(this, project,
                runningWork -> {
                    Date now = new Date();
                    Time endedWork = stopTime(project, runningWork, now);
                    return runningBreakAwareFunction(this, project,
                            (runningBreak) -> WorkStop.ended(endedWork.getId(), stopTime(project, runningBreak, now).getId()),
                            () -> WorkStop.ended(endedWork.getId())
                    );
                },
                WorkStop::notStarted
        );
    }

    public BreakStart startProjectBreak(Project project) {
        return runningWorkAwareFunction(this, project,
                runningWork -> runningBreakAwareFunction(this, project,
                        runningBreak -> BreakStart.breakAlreadyStarted(runningBreak.getId()),
                        () -> BreakStart.started(startTime(project, Time.Type.BREAK).getId())
                ),
                BreakStart::workNotStarted
        );
    }

    public BreakStop endProjectBreak(Project project) {
        return runningBreakAwareFunction(this, project,
                runningBreak -> BreakStop.stopped(stopTime(project, runningBreak).getId()),
                () -> runningWorkAwareFunction(this, project,
                        runningWork -> BreakStop.breakNotStarted(),
                        BreakStop::workNotStarted
                )
        );
    }

    public List<ZoneTime> getZoneTimes(TimeZoneMapper timeZoneMapper, Project project) {
        return namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE project_id = :projectId",
                new MapSqlParameterSource("projectId", project.getId()),
                rowMapper()
        )
                .stream()
                .map(time -> new ZoneTime(timeZoneMapper, time))
                .collect(Collectors.toList());
    }

    public Optional<ZoneTime> getZoneTime(TimeZoneMapper timeZoneMapper, Project project, int timeId) {
        return getTime(project, timeId).map(time -> new ZoneTime(timeZoneMapper, time));
    }

    public Summary getSummary(TimeZoneMapper timeZoneMapper, Project project, Summary.Type type) {
        Summary.Dates dates = type.getDates();
        List<ZoneTime> zoneTimeList = namedJdbc.query(
                "SELECT id, type, start_time, end_time FROM time WHERE end_time IS NOT NULL " +
                        "AND (end_time < :toDate AND start_time > :fromDate) " +
                        "OR (end_time > :toDate AND start_time > :fromDate) " +
                        "OR (end_time < :toDate AND start_time < :fromDate) " +
                        "OR (end_time > :toDate AND start_time < :fromDate) " +
                        "AND project_id = :projectId " +
                        "ORDER BY start_time DESC, end_time DESC",
                new MapSqlParameterSource()
                        .addValue("fromDate", dates.getFrom())
                        .addValue("toDate", dates.getTo())
                        .addValue("projectId", project.getId()),
                rowMapper()
        )
                .stream()
                .map(time -> new ZoneTime(timeZoneMapper, time))
                .collect(Collectors.toList());
        return Summary.create(zoneTimeList);
    }

    public Optional<Time> getRunningProjectTime(Project project, Time.Type type) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE end_time IS NULL AND type = :type AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("type", type.name())
                        .addValue("projectId", project.getId())
        );
    }

    private Time startTime(Project project, Time.Type type) {
        Number key = timeJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("type", type.name())
                .addValue("start_time", new Date())
                .addValue("project_id", project.getId()));
        return getTime(project, key.intValue()).get();
    }

    private Time stopTime(Project project, Time time) {
        return stopTime(project, time, new Date());
    }

    private Time stopTime(Project project, Time zoneTime, Date stopTime) {
        namedJdbc.update(
                "UPDATE time SET end_time = :endTime WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("endTime", stopTime)
                        .addValue("id", zoneTime.getId())
        );
        return getTime(project, zoneTime.getId()).get();
    }

    public Optional<Time> getTime(Project project, int timeId) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE id = :id AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("id", timeId)
                        .addValue("projectId", project.getId())
        );
    }

    private Optional<Time> getSingleTimeFromQuery(String query, MapSqlParameterSource parameters) {
        try {
            return Optional.of(namedJdbc.queryForObject(query, parameters, rowMapper()));
        } catch (IncorrectResultSizeDataAccessException ex) {
            if (ex.getActualSize() == 0) {
                return Optional.empty();
            } else {
                throw new IllegalStateException("Multiple records found");
            }
        }
    }

    private RowMapper<Time> rowMapper() {
        return TIME_ROW_MAPPER;
    }

}
