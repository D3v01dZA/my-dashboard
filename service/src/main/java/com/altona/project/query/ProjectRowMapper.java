package com.altona.project.query;

import com.altona.context.EncryptionContext;
import com.altona.project.Project;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
public class ProjectRowMapper implements RowMapper<Project> {

    @NonNull
    private EncryptionContext encryptionContext;

    @Override
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Project(rs.getInt("id"), rs.getString("name"), encryptionContext);
    }

}
