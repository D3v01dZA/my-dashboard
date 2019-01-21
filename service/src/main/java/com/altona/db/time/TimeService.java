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
        Optional<Project> projectOptional = projectService.getProject(user, projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            Optional<Time> currentlyRunningOptional = currentlyRunningProjectWork(project);
            if (currentlyRunningOptional.isPresent()) {
                return Optional.of(WorkStart.alreadyStarted(currentlyRunningOptional.get().getId()));
            } else {
                Number key = timeJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                        .addValue("type", Time.Type.WORK)
                        .addValue("start_time", new Date())
                        .addValue("project_id", project.getId()));
                return Optional.of(WorkStart.started(key.intValue()));
            }
        } else {
            return Optional.empty();
        }
    }

    public Optional<WorkEnd> endProjectWork(User user, int projectId) {
        Optional<Project> projectOptional = projectService.getProject(user, projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            Optional<Time> currentlyRunningOptional = currentlyRunningProjectWork(project);
            if (currentlyRunningOptional.isPresent()) {
                Time time = currentlyRunningOptional.get();
                namedJdbc.update(
                        "UPDATE time SET end_time = :endTime WHERE id = :id",
                        new MapSqlParameterSource()
                                .addValue("endTime", new Date())
                                .addValue("id", time.getId())
                );
                return Optional.of(WorkEnd.ended(time.getId()));
            } else {
                return Optional.of(WorkEnd.notStarted());
            }
        } else {
            return Optional.empty();
        }
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

    private Optional<Time> currentlyRunningProjectWork(Project project) {
        return getSingleTimeFromQuery(
                "SELECT id, type, start_time, end_time FROM time WHERE end_time IS NULL AND type = 'WORK' AND project_id = :projectId",
                new MapSqlParameterSource("projectId", project.getId())
        );
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
            return Optional.empty();
        }
    }

}
