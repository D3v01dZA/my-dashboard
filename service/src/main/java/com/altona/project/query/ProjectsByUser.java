package com.altona.project.query;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

@AllArgsConstructor
public class ProjectsByUser {

    @NonNull
    private EncryptionContext encryptionContext;

    public List<Project> execute() {
        return encryptionContext.query(
                "SELECT id, name FROM project WHERE user_id = :userId",
                new MapSqlParameterSource("userId", encryptionContext.userId()),
                new ProjectRowMapper(encryptionContext)
        );
    }

}
