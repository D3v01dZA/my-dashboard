package com.altona.repository.db.time.project;

import com.altona.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class ProjectRepository {

    private NamedParameterJdbcTemplate namedJdbc;
    private SimpleJdbcInsert projectJdbcInsert;

    @Autowired
    public ProjectRepository(NamedParameterJdbcTemplate namedJdbc, DataSource dataSource) {
        this.namedJdbc = namedJdbc;
        this.projectJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("project")
                .usingGeneratedKeyColumns("id");
    }

    public List<Project> projects(User user) {
        return namedJdbc.query(
                "SELECT id, name FROM project WHERE user_id = :userId",
                new MapSqlParameterSource("userId", user.getId()),
                (rs, rn) -> new Project(rs.getInt("id"), rs.getString("name"))
        );
    }

    public Optional<Project> project(User user, int projectId) {
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

    public int createProject(User user, Project project) {
        return projectJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", project.getName())
                .addValue("user_id", user.getId()))
                .intValue();
    }

}
