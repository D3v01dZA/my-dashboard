package com.altona.db.time;

import com.altona.db.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TimeService {

    private ProjectService projectService;
    private NamedParameterJdbcTemplate namedJdbc;

    @Autowired
    public TimeService(ProjectService projectService, NamedParameterJdbcTemplate namedJdbc) {
        this.projectService = projectService;
        this.namedJdbc = namedJdbc;
    }

    public Optional<List<Time>> getTimes(User user, Integer projectId) {
        return projectService.getProject(user, projectId)
                .map(project -> namedJdbc.query(
                        "SELECT id, type, start, end FROM time WHERE project_id = :projectId",
                        new MapSqlParameterSource("projectId", project.getId()),
                        (rs, rn) -> new Time(rs.getInt("id"), rs.getString("type"), rs.getDate("start"), rs.getDate("end"))
                ));
    }

}
