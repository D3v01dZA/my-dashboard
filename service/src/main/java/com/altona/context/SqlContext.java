package com.altona.context;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface SqlContext extends NamedParameterJdbcOperations {

    static SqlContext of(NamedParameterJdbcTemplate jdbcOperations) {
        return new UserlessContext(jdbcOperations);
    }

}
