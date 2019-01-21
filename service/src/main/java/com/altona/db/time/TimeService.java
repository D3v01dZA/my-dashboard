package com.altona.db.time;

import com.altona.db.user.User;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.altona.db.time.TimeServiceRunningHelpers.runningBreakAwareFunction;
import static com.altona.db.time.TimeServiceRunningHelpers.runningWorkAwareFunction;

@Service
public class TimeService {

    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rn) -> new Time(rs.getInt("id"), rs.getString("type"), rs.getDate("start_time"), rs.getDate("end_time"));

    private ProjectService projectService;
    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert timeJdbcInsert;

    public TimeService(ProjectService projectService, NamedParameterJdbcTemplate namedJdbc, SimpleJdbcInsert timeJdbcInsert) {
        this.projectService = projectService;
        this.namedJdbc = namedJdbc;
        this.timeJdbcInsert = timeJdbcInsert
                .withTableName("time")
                .usingGeneratedKeyColumns("id");
    }

    public Optional<WorkStart> startProjectWork(User user, int projectId) {
        return runningWorkAwareFunction(this, projectService, user, projectId,
                (project, runningWork) -> WorkStart.alreadyStarted(runningWork.getId()),
                project -> WorkStart.started(startTime(project, Time.Type.WORK).getId())
        );
    }

    public Optional<WorkStop> endProjectWork(User user, int projectId) {
        return runningWorkAwareFunction(this, projectService, user, projectId,
                (project, runningWork) -> {
                    Date now = new Date();
                    Time endedWork = stopTime(project, runningWork, now);
                    return runningBreakAwareFunction(this, project,
                            (runningBreak) -> WorkStop.ended(endedWork.getId(), stopTime(project, runningBreak, now).getId()),
                            () -> WorkStop.ended(endedWork.getId())
                    );
                },
                project -> WorkStop.notStarted()
        );
    }

    public Optional<BreakStart> startProjectBreak(User user, int projectId) {
        return runningWorkAwareFunction(this, projectService, user, projectId,
                (project, runningWork) -> runningBreakAwareFunction(this, project,
                        runningBreak -> BreakStart.breakAlreadyStarted(runningBreak.getId()),
                        () -> BreakStart.started(startTime(project, Time.Type.BREAK).getId())
                ),
                project -> BreakStart.workNotStarted()
        );
    }

    public Optional<BreakStop> endProjectBreak(User user, int projectId) {
        return runningBreakAwareFunction(this, projectService, user, projectId,
                (project, runningBreak) -> BreakStop.stopped(stopTime(project, runningBreak).getId()),
                project -> runningWorkAwareFunction(this, project,
                        runningWork -> BreakStop.breakNotStarted(),
                        BreakStop::workNotStarted
                )
        );
    }

    public Optional<List<Time>> getTimes(User user, int projectId) {
        return projectService.getProject(user, projectId)
                .map(project -> namedJdbc.query(
                        "SELECT id, type, start_time, end_time FROM time WHERE project_id = :projectId",
                        new MapSqlParameterSource("projectId", project.getId()),
                        TIME_ROW_MAPPER
                ));
    }

    public Optional<Time> getTime(User user, int projectId, int timeId) {
        return projectService.getProject(user, projectId)
                .flatMap(project -> getTime(project, timeId));
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

    private Time stopTime(Project project, Time time, Date stopTime) {
        namedJdbc.update(
                "UPDATE time SET end_time = :endTime WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("endTime", stopTime)
                        .addValue("id", time.getId())
        );
        return getTime(project, time.getId()).get();
    }

    private Optional<Time> getTime(Project project, int timeId) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE id = :id AND project_id = :projectId",
                new MapSqlParameterSource()
                        .addValue("id", timeId)
                        .addValue("projectId", project.getId())
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
