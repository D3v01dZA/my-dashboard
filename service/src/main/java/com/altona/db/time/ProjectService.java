package com.altona.db.time;

import com.altona.db.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert projectJdbcInsert;

    @Autowired
    public ProjectService(NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.namedJdbc = namedJdbc;
        this.projectJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("project")
                .usingGeneratedKeyColumns("id");
    }

    public List<Project> getProjects(User user) {
        return namedJdbc.query(
                "SELECT id, name FROM project WHERE user_id = :userId",
                new MapSqlParameterSource("userId", user.getId()),
                (rs, rn) -> new Project(rs.getInt("id"), rs.getString("name"))
        );
    }

    public Optional<Project> getProject(User user, int projectId) {
        try {
            return Optional.of(namedJdbc.queryForObject(
                    "SELECT id, name FROM project WHERE id = :projectId AND user_id = :userId",
                    new MapSqlParameterSource()
                            .addValue("projectId", projectId)
                            .addValue("userId", user.getId()),
                    (rs, rn) -> new Project(rs.getInt("id"), rs.getString("name"))
            ));
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Project createProject(User user, Project project) {
        Number key = projectJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", project.getName())
                .addValue("user_id", user.getId()));
        return getProject(user, key.intValue()).get();
    }

}
