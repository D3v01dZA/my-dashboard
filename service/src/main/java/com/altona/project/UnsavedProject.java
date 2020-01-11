package com.altona.project;

import com.altona.context.EncryptionContext;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Map;

@AllArgsConstructor
public class UnsavedProject {

    @NonNull
    private String name;

    public Project save(EncryptionContext encryptionContext) {
        try {
            Map<String, Object> stringObjectMap = encryptionContext.queryForMap(
                    "INSERT INTO project (name, user_id) VALUES (:name, :userId) RETURNING id",
                    new MapSqlParameterSource()
                            .addValue("name", name)
                            .addValue("userId", encryptionContext.userId())
            );
            Integer id = (Integer) stringObjectMap.get("id");
            return new Project(encryptionContext, id, name);
        } catch (IncorrectResultSizeDataAccessException ex) {
            throw new IllegalStateException(String.format("Inserting lead to %s rows returned", ex.getActualSize()));
        }
    }

}
