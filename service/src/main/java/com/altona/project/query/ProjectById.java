package com.altona.project.query;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Optional;

@AllArgsConstructor
public class ProjectById {

    @NonNull
    private EncryptionContext encryptionContext;

    private int id;

    public Optional<Project> execute() {
        try {
            return Optional.of(encryptionContext.queryForObject(
                    "SELECT id, name FROM project WHERE id = :projectId AND user_id = :userId",
                    new MapSqlParameterSource()
                            .addValue("projectId", id)
                            .addValue("userId", encryptionContext.userId()),
                    new ProjectRowMapper(encryptionContext)
            ));
        } catch (IncorrectResultSizeDataAccessException ex) {
            return Optional.empty();
        }
    }

}
